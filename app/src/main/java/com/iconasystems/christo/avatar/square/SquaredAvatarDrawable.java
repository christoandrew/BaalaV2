package com.iconasystems.christo.avatar.square;

/*
 * Copyright 2014 Pedro Álvarez Fernández <pedroafa@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.iconasystems.christo.utils.SquareCanvas;
import com.iconasystems.christo.utils.SquareCanvasProvider;


public class SquaredAvatarDrawable extends Drawable {

    private final SquareCanvasProvider mSquareCanvasProvider;
    private final Bitmap mBitmap;

    public SquaredAvatarDrawable(SquareCanvasProvider squareCanvasProvider, Bitmap bitmap) {
        mSquareCanvasProvider = squareCanvasProvider;
        mBitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        SquareCanvas squareCanvas = mSquareCanvasProvider.getSquareCanvas(canvas);

        squareCanvas.drawBitmap(mBitmap);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
