
/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui.widgets.menus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.ui.widgets.UIWidget;
import org.mozilla.vrbrowser.utils.ViewUtils;

import java.util.ArrayList;

public abstract class MenuWidget extends UIWidget {
    protected MenuAdapter mAdapter;
    protected ListView mListView;
    protected View menuContainer;

    public MenuWidget(Context aContext, @LayoutRes int layout) {
        super(aContext);
        initialize(aContext, layout, null);
    }

    public MenuWidget(Context aContext, @LayoutRes int layout, ArrayList<MenuItem> aItems) {
        super(aContext);
        initialize(aContext, layout, aItems);
    }

    private void initialize(Context aContext, @LayoutRes int layout, ArrayList<MenuItem> aItems) {
        inflate(aContext, layout, this);
        mListView = findViewById(R.id.menuListView);
        menuContainer = findViewById(R.id.menuContainer);


        mAdapter = new MenuAdapter(aContext, aItems);
        mListView.setAdapter(mAdapter);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setFastScrollAlwaysVisible(false);
    }

    public void updateMenuItems(ArrayList<MenuItem> aItems) {
        mAdapter.mItems = aItems;
        mAdapter.notifyDataSetChanged();
    }

    public void setSelectedItem(int aPosition) {
        mListView.setItemChecked(aPosition, true);
    }

    public int getSelectedItem() {
        return mListView.getCheckedItemPosition();
    }

    public static class MenuItem {
        String mText;
        int mImageId;
        Runnable mCallback;

        public MenuItem(String aString, int aImage, Runnable aCallback) {
            mText = aString;
            mImageId = aImage;
            mCallback = aCallback;
        }
    }

    public class MenuAdapter extends BaseAdapter implements OnHoverListener {
        private Context mContext;
        private ArrayList<MenuItem> mItems;
        private LayoutInflater mInflater;
        private int singleItemDrawable;
        private int firstItemDrawable;
        private int lastItemDrawable;
        private int regularItemDrawable;
        private int layoutId;

        MenuAdapter(Context aContext, ArrayList<MenuItem> aItems) {
            mContext = aContext;
            mItems = aItems != null ? aItems : new ArrayList<>();
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            firstItemDrawable = R.drawable.menu_item_background_first;
            lastItemDrawable = R.drawable.menu_item_background_last;
            regularItemDrawable = R.drawable.menu_item_background;
            singleItemDrawable = R.drawable.menu_item_background_single;
            layoutId = R.layout.menu_item_image_text;
        }

        public void updateBackgrounds(int first, int last, int regular, int single) {
            firstItemDrawable = first;
            lastItemDrawable = last;
            regularItemDrawable = regular;
            singleItemDrawable = single;
        }

        public void updateLayoutId(int aLayoutId) {
            layoutId = aLayoutId;
        }


        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(layoutId, parent, false);
                view.setOnHoverListener(this);
                ViewUtils.setStickyClickListener(view, v -> {
                    setSelectedItem(position);
                    MenuItem item = mItems.get(position);
                    if (item.mCallback != null) {
                        item.mCallback.run();
                    }
                });
            }
            view.setTag(R.string.position_tag, position);
            if (mItems.size() == 1) {
                view.setBackgroundResource(singleItemDrawable);
            } else if (position == 0) {
                view.setBackgroundResource(firstItemDrawable);
            } else if (position == mItems.size() - 1) {
                view.setBackgroundResource(lastItemDrawable);
            } else {
                view.setBackgroundResource(regularItemDrawable);
            }

            MenuItem item = mItems.get(position);

            TextView textView = view.findViewById(R.id.listItemText);
            ImageView imageView = view.findViewById(R.id.listItemImage);

            textView.setText(item.mText);
            if (imageView != null) {
                if (item.mImageId > 0) {
                    imageView.setImageResource(item.mImageId);
                } else {
                    imageView.setVisibility(View.GONE);
                    textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
                }
            }

            if (item.mCallback == null) {
                textView.setTextColor(mContext.getColor(R.color.rhino));
            }

            View separator = view.findViewById(R.id.listItemSeparator);
            if (separator != null) {
                separator.setVisibility(item.mCallback != null ? View.GONE : View.VISIBLE);
            }

            return view;
        }

        @Override
        public boolean onHover(View view, MotionEvent event) {
            int position = (int)view.getTag(R.string.position_tag);
            if (!isEnabled(position)) {
                return false;
            }

            MenuItem item = mItems.get(position);
            if (item.mCallback == null) {
                return false;
            }

            TextView label = view.findViewById(R.id.listItemText);
            ImageView image = view.findViewById(R.id.listItemImage);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    view.setHovered(true);
                    label.setHovered(true);
                    label.setShadowLayer(label.getShadowRadius(), label.getShadowDx(), label.getShadowDy(), mContext.getColor(R.color.text_shadow_light));
                    if (image != null) {
                        image.setHovered(true);
                    }
                    return true;

                case MotionEvent.ACTION_HOVER_EXIT:
                    view.setHovered(false);
                    label.setShadowLayer(label.getShadowRadius(), label.getShadowDx(), label.getShadowDy(), mContext.getColor(R.color.text_shadow));
                    label.setHovered(false);
                    if (image != null) {
                        image.setHovered(false);
                    }
                    return true;
            }

            return false;
        }
    }

}
