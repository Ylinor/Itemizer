package com.onaple.itemizer.data.beans.crafts;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeumimTo on 23.3.2019.
 */
@ConfigSerializable

public class CraftsRoot {

    public CraftsRoot() {
    }

    @Setting("crafts")
    private List<ICraftRecipes> craftingRecipes = new ArrayList<>();

    public List<ICraftRecipes> getCraftingRecipes() {
        return craftingRecipes;
    }
}
