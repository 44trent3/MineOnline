package gg.codie.mineonline.gui.rendering.font;

import java.util.List;
import java.util.Map;

import gg.codie.mineonline.gui.font.FontType;
import gg.codie.mineonline.gui.font.GUIText;
import gg.codie.mineonline.gui.rendering.DisplayManager;
import gg.codie.mineonline.gui.rendering.shaders.FontShader;
import org.lwjgl.opengl.*;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class FontRenderer {

    private FontShader shader;

    public FontRenderer() {
        shader = new FontShader();
    }

    public void render(Map<FontType, List<GUIText>> texts){
        prepare();
        for(FontType font : texts.keySet()){
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getTextureAtlas());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

            for(GUIText text : texts.get(font)){
                renderText(text);
            }
        }
        endRendering();
    }

    public void cleanUp(){
        shader.cleanUp();
    }

    private void prepare(){
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glViewport(DisplayManager.getXBuffer(), DisplayManager.getYBuffer(), (int)(DisplayManager.getDefaultWidth() * DisplayManager.getScale()), (int)(DisplayManager.getDefaultHeight() * DisplayManager.getScale()));

        shader = new FontShader();
        shader.start();
    }

    private void renderText(GUIText text){
        GL30.glBindVertexArray(text.getMesh());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        shader.loadColour(new Vector3f());
        shader.loadAlpha(0.5f);
        shader.loadTranslation(new Vector2f(text.getPosition().x + (2 / (float)DisplayManager.getDefaultWidth()), text.getPosition().y + (2 / (float)DisplayManager.getDefaultHeight())));
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());

        shader.loadColour(text.getColour());
        shader.loadAlpha(1);
        shader.loadTranslation(text.getPosition());
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    private void endRendering(){
        shader.stop();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

}