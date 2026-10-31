package com.cauansilva.arcanecontrol.core.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class TeclasControlo {
    public static final KeyMapping TECLA_Z = new KeyMapping("key.minezin.z", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "category.minezin.magias");
    public static final KeyMapping TECLA_X = new KeyMapping("key.minezin.x", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.minezin.magias");
    public static final KeyMapping TECLA_C = new KeyMapping("key.minezin.c", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.minezin.magias");
    public static final KeyMapping TECLA_V = new KeyMapping("key.minezin.v", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.minezin.magias");
    public static final KeyMapping TECLA_R = new KeyMapping("key.minezin.r", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.minezin.magias");
}
