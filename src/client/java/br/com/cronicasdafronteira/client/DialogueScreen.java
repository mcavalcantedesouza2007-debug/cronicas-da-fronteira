package br.com.cronicasdafronteira.client;

import br.com.cronicasdafronteira.CronicasMod;
import br.com.cronicasdafronteira.DialogueData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import java.util.ArrayList;
import java.util.List;

/** Tela com retrato, identidade, árvore de diálogo e respostas clicáveis (26.3, GuiGraphicsExtractor). */
public final class DialogueScreen extends Screen {
    private final String role;
    private final String npcName;
    private String currentNode = "inicio";
    private final String temperament;
    private static final int GOLD = 0xffd9b66b;
    private static final int TEXT = 0xfff3e6c6;

    public DialogueScreen(Villager villager) {
        super(Component.literal("Crônicas da Fronteira: Diálogo"));
        role = CronicasMod.roleOf(villager);
        npcName = CronicasMod.nameOf(villager);
        String[] traits = {"prudente","compassivo","inquieto","reservado"};
        temperament = traits[Math.floorMod(villager.getUUID().hashCode(),traits.length)];
    }
    @Override protected void init() {
        super.init();
        rebuildOptions();
    }
    private void rebuildOptions() {
        clearWidgets();
        DialogueData.Node node = DialogueData.node(role,currentNode);
        int w = Math.min(410, this.width - 30);
        int left = (width-w)/2;
        int y = Math.max(15, (height-Math.min(248,height-10))/2 + 134);
        int count = Math.min(5,node.options().size());
        for (int i=0;i<count;i++) {
            DialogueData.Option option=node.options().get(i);
            String number=(i+1)+".  ";
            String label=number+option.caption();
            addRenderableWidget(Button.builder(Component.literal(label), b -> answer(option))
                .bounds(left+16,y+i*21,w-32,19).build());
        }
        if(node.options().isEmpty())
            addRenderableWidget(Button.builder(Component.literal("Terminar conversa"), b -> onClose())
                .bounds(left+16,y,w-32,20).build());
    }
    private void answer(DialogueData.Option option) {
        Minecraft client=Minecraft.getInstance();
        if(client.getConnection()!=null)client.getConnection().sendCommand("cronicas escolher "+option.id());
        // Quando há gasto, recompensa ou decisão moral, o servidor confirma no chat;
        // fechar evita apresentar como aceita uma escolha que pode ser rejeitada.
        if(!option.effect().equals("none") || option.next().equals("fim")) {
            onClose();return;
        }
        currentNode=option.next();
        rebuildOptions();
    }
    @Override public void onClose() {minecraft.gui.setScreen(null);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        int w=Math.min(410,width-30);
        int h=Math.min(248,height-10);
        int left=(width-w)/2;
        int top=Math.max(5,(height-h)/2);
        g.fill(0,0,width,height,0xb800060b);
        g.fill(left-2,top-2,left+w+2,top+h+2,0xff96754d);
        g.fill(left,top,left+w,top+h,0xff1c1b20);
        g.fill(left+2,top+2,left+w-2,top+25,0xff423426);
        g.text(font,"CRÔNICAS DA FRONTEIRA  •  DIÁLOGO",left+10,top+9,GOLD,true);
        Identifier face=Identifier.fromNamespaceAndPath(CronicasMod.ID,"textures/gui/"+role+".png");
        g.blit(RenderPipelines.GUI_TEXTURED,face,left+15,top+31,0,0,64,64,64,64);
        g.outline(left+13,top+29,68,68,GOLD);
        g.text(font,npcName,left+91,top+34,GOLD,true);
        g.text(font,DialogueData.profession(role)+" • "+temperament,left+91,top+49,0xffbba88c,false);
        g.text(font,"Escolhas moldam reputação e relações.",left+91,top+66,0xffd1c4ab,false);
        g.fill(left+15,top+101,left+w-15,top+102,0xff705235);
        DialogueData.Node node=DialogueData.node(role,currentNode);
        int yy=top+108;
        for(String line:wrap(node.line(),Math.max(21,(w-37)/6))) {
            if(yy>top+127)break;
            g.text(font,line,left+18,yy,TEXT,false);
            yy+=12;
        }
        g.text(font,"ESCOLHA SUA RESPOSTA",left+16,top+124,0xffaa936e,true);
        super.extractRenderState(g,mouseX,mouseY,delta);
    }
    private static List<String> wrap(String src,int cols) {
        List<String> lines=new ArrayList<>();StringBuilder line=new StringBuilder();
        for(String part:src.split(" ")) {
            if(!line.isEmpty() && line.length()+part.length()+1>cols) {
                lines.add(line.toString());line.setLength(0);
            }
            if(!line.isEmpty())line.append(' ');
            line.append(part);
        }
        if(!line.isEmpty())lines.add(line.toString());
        return lines;
    }
}
