package com.tv.player;

import android.content.Context;

/**
 * @author aim
 * @date :2020/12/24
 * @description:
 */
public abstract class PlayerFactory<P extends AbstractPlayer> {
    public abstract P createPlayer(Context context);
}