package com.voyager.edition.book

import net.minecraft.ChatFormatting
import net.minecraft.world.item.ItemStack

object LorebookRegistry {

    val VANCE_BOOK: ItemStack by lazy { buildVanceBook() }
    val LYRA_BOOK: ItemStack by lazy { buildLyraBook() }

    // ─── Vance: Voyager Foundation lore (the "official" truth) ───────────────

    private fun buildVanceBook(): ItemStack =
        VoyagerLorebooks.builder(
            "História, Regras e Princípios da Fundação Voyager",
            "Capitão Vance"
        )
        .tableOfContents {
            chapter("Origens", 2)
            chapter("Regras Fundamentais", 3)
            chapter("Conflitos com o Hypnos", 4)
            chapter("Executivos", 5)
            chapter("Epílogo", 6)
        }
        .page {
            title("Origens")
            text("Antes da Fundação, Keitai era caos. Fendas devoravam ilhas inteiras.")
            newLine()
            text("Dr. Elias Thorn descobriu que a Redstone podia estabilizar o vazio.")
            newLine(2)
            text("2155: A Fundação Voyager é criada.", ChatFormatting.DARK_BLUE)
        }
        .page {
            title("Regras Fundamentais")
            text("1. Ordem acima de tudo.\n", ChatFormatting.GOLD)
            text("2. Progresso não tem preço.\n", ChatFormatting.GOLD)
            text("3. Anomalias devem ser contidas.\n", ChatFormatting.GOLD)
            separator()
            text("[Páginas 3-4 redigidas pela censura]", ChatFormatting.DARK_GRAY)
        }
        .page {
            title("Conflitos com o Team Hypnos")
            text("2168 – Ataque ao Gerador Central: 3 ilhas perdidas, 50 civis mortos.")
            newLine()
            text("2172 – Batalha de Centauri: 15 Guardiões caídos.")
            newLine(2)
            text(
                "O Hypnos não luta por liberdade. Luta para destruir a única coisa que nos mantém vivos.",
                ChatFormatting.DARK_RED
            )
        }
        .page {
            title("Executivos e Figuras")
            text("Dr. Elias Thorn — Fundador\n")
            text("Professor Redwood — Ciência\n")
            text("Capitão Vance — Defesa\n")
            text("Admiral Kael — Segurança †\n")
            text("Dra. Mira Voss — Deserto (irmã de Lyra)\n")
            separator()
            text("Herói lendário: Cadete Zero", ChatFormatting.GOLD)
        }
        .page {
            title("Epílogo")
            text("Cadete, a Fundação é o escudo de Keitai.")
            newLine()
            text("Ignore as mentiras do Hypnos.")
            newLine(2)
            text("Ordem acima de tudo.", ChatFormatting.DARK_BLUE)
            newLine(2)
            pageLink("← Voltar ao Sumário", 1)
        }
        .build()
        .toItemStack()

    // ─── Lyra: Team Hypnos philosophy (the "real" truth) ─────────────────────

    private fun buildLyraBook(): ItemStack =
        VoyagerLorebooks.builder(
            "Hypnos, Filosofia e um Futuro Melhor para Keitai",
            "Lyra"
        )
        .tableOfContents {
            chapter("O Sono da Fundação", 2)
            chapter("A Cicatriz Vermelha", 3)
            chapter("Ultra Beasts como Anticorpos", 4)
            chapter("O Futuro sem Correntes", 5)
        }
        .page {
            title("O Sono da Fundação")
            text("Keitai não é um paraíso.")
            newLine()
            text(
                "É uma prisão flutuante construída sobre o cadáver de um mundo vivo.",
                ChatFormatting.DARK_PURPLE
            )
            newLine(2)
            text("A Redstone não salva. Ela queima.")
        }
        .page {
            title("A Cicatriz Vermelha")
            text("Cada gerador que a Fundação planta é uma ferida.")
            newLine()
            text(
                "Cada fenda que se abre é o mundo tentando respirar.",
                ChatFormatting.DARK_PURPLE
            )
            newLine(2)
            text("E vocês chamam isso de progresso.")
        }
        .page {
            title("Ultra Beasts como Anticorpos")
            text("Elas não invadem. Elas respondem.")
            newLine()
            text(
                "São o sistema imunológico de uma realidade que estamos envenenando.",
                ChatFormatting.DARK_PURPLE
            )
            separator()
            text("Abram os olhos antes que seja tarde.")
        }
        .page {
            title("O Futuro sem Correntes")
            text("Imagine um Keitai onde Pokémon e humanos sonham juntos.")
            newLine()
            text("Sem Pokébolas. Sem Redstone. Sem hierarquia.", ChatFormatting.DARK_PURPLE)
            newLine(2)
            text("Esse é o sonho do Hypnos.", ChatFormatting.LIGHT_PURPLE)
            newLine()
            text("E ele é belo.")
            newLine(2)
            pageLink("← Sumário", 1)
        }
        .build()
        .toItemStack()
}
