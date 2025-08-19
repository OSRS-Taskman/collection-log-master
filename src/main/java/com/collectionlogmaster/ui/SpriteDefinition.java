package com.collectionlogmaster.ui;

import lombok.Getter;
import net.runelite.client.game.SpriteOverride;

@Getter
public class SpriteDefinition implements SpriteOverride
{
    private int spriteId;
    private String fileName;
}
