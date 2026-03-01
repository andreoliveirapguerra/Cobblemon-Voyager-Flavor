package com.voyager.edition.book

import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.Filterable
import net.minecraft.world.item.component.WrittenBookContent

/**
 * VoyagerLorebooks — Builder/DSL for creating rich in-game written books.
 *
 * ## Features
 * - Table of contents with **clickable chapter navigation** (uses `CHANGE_PAGE` click event).
 * - **Custom image support** via bitmap font characters (requires a resource-pack font definition).
 * - **Clickable external URL links** (uses `OPEN_URL` click event).
 * - Rich text formatting: bold, italic, colour, underline, etc.
 *
 * ## Custom Font Images
 * Define a bitmap font at:
 *   `assets/voyager-flavor/font/lorebook.json`
 * Map unicode private-use characters (e.g. `\uE001`) to texture regions.
 * Pass those characters to [PageScope.image] and the correct font will be applied automatically.
 *
 * Example font JSON (`assets/voyager-flavor/font/lorebook.json`):
 * ```json
 * {
 *   "providers": [
 *     {
 *       "type": "bitmap",
 *       "file": "voyager-flavor:textures/font/lorebook_sprites.png",
 *       "ascent": 7,
 *       "height": 16,
 *       "chars": ["\uE001\uE002\uE003"]
 *     }
 *   ]
 * }
 * ```
 *
 * ## Usage Example
 * ```kotlin
 * val stack = VoyagerLorebooks.builder("Voyager Chronicles", "Professor Vega")
 *     .tableOfContents {
 *         // Page numbers account for the TOC being page 1; chapters start at page 2.
 *         chapter("Prologue",      pageNumber = 2)
 *         chapter("The Departure", pageNumber = 3)
 *         chapter("Useful Links",  pageNumber = 4)
 *     }
 *     .page {                              // page 2 — chapter 1
 *         title("Prologue")
 *         image('\uE001')                 // full-width illustration from bitmap font
 *         newLine()
 *         text("In the beginning, the Voyager region was shrouded in mystery…")
 *     }
 *     .page {                              // page 3 — chapter 2
 *         title("The Departure")
 *         separator()
 *         text("You set out at dawn, your partner Pokémon at your side.")
 *     }
 *     .page {                              // page 4 — chapter 3
 *         title("Useful Links")
 *         newLine()
 *         link("Voyager Wiki", "https://example.com/wiki")
 *         newLine()
 *         pageLink("← Back to Contents", targetPage = 1)
 *     }
 *     .build()
 *     .toItemStack()
 * ```
 */
class VoyagerLorebooks private constructor(
    val title: String,
    val author: String,
    val pages: List<Component>
) {

    companion object {
        /**
         * [ResourceLocation] of the custom bitmap font used for lorebook images.
         * Corresponds to `assets/voyager-flavor/font/lorebook.json` in the resource pack.
         */
        val LOREBOOK_FONT: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath("voyager-flavor", "lorebook")

        /** Entry point for the builder DSL. */
        fun builder(title: String, author: String): Builder = Builder(title, author)
    }

    /**
     * Wraps this lorebook into a [WrittenBookContent] and returns a ready-to-use
     * [ItemStack] of [Items.WRITTEN_BOOK].
     */
    fun toItemStack(): ItemStack {
        val stack = ItemStack(Items.WRITTEN_BOOK)
        val content = WrittenBookContent(
            Filterable.passThrough(title),
            author,
            /* generation = */ 0,
            pages.map { Filterable.passThrough(it) },
            /* resolved = */ true
        )
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content)
        return stack
    }

    // ─── Builder ──────────────────────────────────────────────────────────────

    class Builder internal constructor(
        private val title: String,
        private val author: String
    ) {
        private val pages = mutableListOf<Component>()
        private val tocEntries = mutableListOf<TocEntry>()

        internal data class TocEntry(val label: String, val pageNumber: Int)

        /**
         * Defines the table of contents. Call [TocScope.chapter] for each entry.
         * The TOC page is automatically prepended as **page 1**; make sure chapter
         * [pageNumber] values account for this offset (chapters begin at page 2+).
         */
        fun tableOfContents(block: TocScope.() -> Unit): Builder {
            TocScope(tocEntries).block()
            return this
        }

        /**
         * Appends a page built with the [PageScope] DSL.
         */
        fun page(block: PageScope.() -> Unit): Builder {
            val scope = PageScope()
            scope.block()
            pages.add(scope.build())
            return this
        }

        /**
         * Appends a pre-built [Component] as a raw page (escape hatch for advanced use).
         */
        fun rawPage(component: Component): Builder {
            pages.add(component)
            return this
        }

        /** Finalises the lorebook and returns a [VoyagerLorebooks] instance. */
        fun build(): VoyagerLorebooks {
            val allPages = mutableListOf<Component>()
            if (tocEntries.isNotEmpty()) {
                allPages.add(buildTocPage())
            }
            allPages.addAll(pages)
            return VoyagerLorebooks(title, author, allPages)
        }

        private fun buildTocPage(): Component {
            val toc: MutableComponent = Component.empty()
                .append(
                    Component.literal("Contents\n\n")
                        .withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLACK))
                )
            for (entry in tocEntries) {
                toc.append(
                    Component.literal("▶ ${entry.label}\n")
                        .withStyle(
                            Style.EMPTY
                                .withColor(ChatFormatting.DARK_BLUE)
                                .withUnderlined(true)
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.CHANGE_PAGE,
                                        entry.pageNumber.toString()
                                    )
                                )
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Go to: ${entry.label}")
                                    )
                                )
                        )
                )
            }
            return toc
        }
    }

    // ─── Table of Contents Scope ──────────────────────────────────────────────

    class TocScope internal constructor(private val entries: MutableList<Builder.TocEntry>) {
        /**
         * Registers a chapter entry in the table of contents.
         *
         * @param label      The display name shown in the TOC.
         * @param pageNumber The 1-based page number the reader jumps to on click.
         *                   Remember: page **1** is the TOC itself.
         */
        fun chapter(label: String, pageNumber: Int) {
            entries.add(Builder.TocEntry(label, pageNumber))
        }
    }

    // ─── Page Scope ───────────────────────────────────────────────────────────

    /**
     * DSL scope for composing a single book page.
     *
     * Each method appends content to the page and returns `this` for chaining.
     * Call [build] (done automatically by [Builder.page]) to obtain the finished [Component].
     */
    class PageScope internal constructor() {
        private val root: MutableComponent = Component.empty()

        /**
         * Appends bold black title text followed by a newline.
         */
        fun title(text: String): PageScope {
            root.append(
                Component.literal("$text\n")
                    .withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLACK))
            )
            return this
        }

        /**
         * Appends plain body text in the given [ChatFormatting] colour.
         */
        fun text(text: String, formatting: ChatFormatting = ChatFormatting.BLACK): PageScope {
            root.append(Component.literal(text).withStyle(formatting))
            return this
        }

        /**
         * Appends any pre-built [Component] directly.
         */
        fun component(component: Component): PageScope {
            root.append(component)
            return this
        }

        /**
         * Appends a clickable external URL link.
         *
         * @param label Visible link text shown to the reader.
         * @param url   Target URL (must begin with `http://` or `https://`).
         */
        fun link(label: String, url: String): PageScope {
            root.append(
                Component.literal(label)
                    .withStyle(
                        Style.EMPTY
                            .withColor(ChatFormatting.BLUE)
                            .withUnderlined(true)
                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
                            .withHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.literal(url)
                                )
                            )
                    )
            )
            return this
        }

        /**
         * Appends an image rendered via a custom bitmap font character.
         *
         * The character must be mapped to a texture in the font definition at
         * `assets/voyager-flavor/font/lorebook.json` (or whichever [font] you supply).
         *
         * @param char The unicode character (e.g. `'\uE001'`) mapped to the image.
         * @param font The [ResourceLocation] of the custom font; defaults to [LOREBOOK_FONT].
         */
        fun image(char: Char, font: ResourceLocation = LOREBOOK_FONT): PageScope {
            root.append(
                Component.literal(char.toString())
                    .withStyle(Style.EMPTY.withFont(font))
            )
            return this
        }

        /**
         * Appends one or more newlines.
         *
         * @param count Number of newlines to insert (default 1).
         */
        fun newLine(count: Int = 1): PageScope {
            root.append(Component.literal("\n".repeat(count)))
            return this
        }

        /**
         * Appends a decorative horizontal separator followed by a newline.
         */
        fun separator(): PageScope {
            root.append(
                Component.literal("─────────────\n")
                    .withStyle(ChatFormatting.GRAY)
            )
            return this
        }

        /**
         * Appends a clickable link that navigates to another page within the same book.
         *
         * @param label      Visible link text.
         * @param targetPage 1-based page number to jump to.
         */
        fun pageLink(label: String, targetPage: Int): PageScope {
            root.append(
                Component.literal(label)
                    .withStyle(
                        Style.EMPTY
                            .withColor(ChatFormatting.DARK_AQUA)
                            .withUnderlined(true)
                            .withClickEvent(
                                ClickEvent(
                                    ClickEvent.Action.CHANGE_PAGE,
                                    targetPage.toString()
                                )
                            )
                            .withHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("Go to page $targetPage")
                                )
                            )
                    )
            )
            return this
        }

        internal fun build(): Component = root
    }
}
