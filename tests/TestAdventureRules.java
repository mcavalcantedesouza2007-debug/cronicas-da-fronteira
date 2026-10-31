import br.com.cronicasdafronteira.AdventureRules;

import java.util.HashSet;
import java.util.Set;

public class TestAdventureRules {
    private static int checks = 0;
    private static void expect(boolean ok, String msg) {
        checks++;
        if (!ok) throw new AssertionError("Falhou: " + msg);
    }
    public static void main(String[] args) {
        expect(AdventureRules.STORY.size() == 12, "12 capítulos principais");
        expect(AdventureRules.SIDE.size() == 18, "18 contratos secundários");
        Set<String> ids = new HashSet<>();
        for (var q : AdventureRules.STORY) {
            expect(ids.add(q.id()), "id principal único: " + q.id());
            expect(q.quantity() > 0 && q.xp() > 0, "quantidade/XP válido");
            expect(q.goal() == AdventureRules.Goal.BOSS || q.target().startsWith("minecraft:"), "id válido de item ou mob");
            expect(!q.title().isBlank() && !q.summary().isBlank(), "missão com texto");
        }
        for (var q : AdventureRules.SIDE) {
            expect(ids.add(q.id()), "id lateral único: " + q.id());
            expect(q.quantity() > 0 && q.xp() > 0, "quantidade/XP lateral válida");
            expect(q.target().startsWith("minecraft:"), "contrato com item/mob vanilla");
        }
        expect(AdventureRules.STORY.get(4).goal() == AdventureRules.Goal.BOSS, "primeiro vilão no capítulo 5");
        expect(AdventureRules.STORY.get(11).goal() == AdventureRules.Goal.BOSS, "vilã no capítulo 12");
        expect(AdventureRules.moralDecisionRequired(4) && AdventureRules.moralDecisionRequired(7)
          && AdventureRules.moralDecisionRequired(11), "três ramificações morais");
        expect(!AdventureRules.moralDecisionRequired(3), "capítulo regular sem decisão forçada");
        expect(AdventureRules.clampHonor(-140) == -100 && AdventureRules.clampHonor(146) == 100, "limites da honra");
        expect(AdventureRules.clampRelation(-200) == -100 && AdventureRules.clampRelation(400) == 100, "limites de vínculo");
        expect(AdventureRules.ending(100, "selar").equals("O GUARDIÃO DA FRONTEIRA"), "final heroico");
        expect(AdventureRules.ending(-90, "selar").equals("A PAZ PELO MEDO"), "final sombrio");
        expect(AdventureRules.ending(100, "dominar").equals("O TRONO DAS SOMBRAS"), "escolha prevalece sobre honra");
        expect(AdventureRules.ending(0, "selar").equals("A PAZ INCERTA"), "final neutro");
        expect(AdventureRules.honorTitle(80).equals("Lenda da Fronteira"), "reputação positiva");
        expect(AdventureRules.honorTitle(-85).equals("Inimigo da Fronteira"), "reputação negativa");
        expect(AdventureRules.mobTemperament("Lobo", 12345).equals(AdventureRules.mobTemperament("Lobo", 12345)),
            "temperamento estável por criatura");
        System.out.println("APROVADO: " + checks + " verificações das regras de RPG.");
        System.out.println("Não cobre execução/compilação do Fabric ou teste no Minecraft.");
    }
}
