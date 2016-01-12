package com.super2k.tiledspriteengine;

import com.nucleus.actor.ActorItem;
import com.nucleus.actor.ActorResolver;

/**
 * Resolves the class to be used for a logic object
 * 
 * @author Richard Sahlin
 *
 */
public class SuperSpriteResolver implements ActorResolver {

    public enum Logics {
        AFSPRITE(AFSprite.class);

        private final Class clazz;

        private Logics(Class clazz) {
            this.clazz = clazz;
        }

        public ActorItem getInstance() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
            return (ActorItem) clazz.newInstance();
        }

    }

    @Override
    public ActorItem getActor(String id) {
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
