package com.super2k.tiledspriteengine;

import com.nucleus.logic.LogicItem;
import com.nucleus.logic.LogicResolver;

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

        public LogicItem getInstance() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            return (LogicItem) clazz.newInstance();
        }

    }

    @Override
    public LogicItem getLogic(String id) {
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
