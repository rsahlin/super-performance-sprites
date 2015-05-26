package com.super2k.tiledspriteengine;

import com.graphicsengine.sprite.Sprite.Logic;
import com.graphicsengine.sprite.SpriteController.LogicResolver;

/**
 * Resolves the class to be used for a logic object
 * 
 * @author Richard Sahlin
 *
 */
public class SuperSpriteResolver implements LogicResolver {

    public enum Logics {
        AFSPRITE(AFSprite.class);

        private final Class clazz;

        private Logics(Class clazz) {
            this.clazz = clazz;
        }

        public Logic getInstance() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            return (Logic) clazz.newInstance();
        }

    }

    @Override
    public Logic getLogic(String id) {
        try {
            return Logics.valueOf(id).getInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
