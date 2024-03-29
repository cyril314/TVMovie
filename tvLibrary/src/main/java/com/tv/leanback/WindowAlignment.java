package com.tv.leanback;


import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;
import static com.tv.leanback.BaseGridView.WINDOW_ALIGN_BOTH_EDGE;
import static com.tv.leanback.BaseGridView.WINDOW_ALIGN_HIGH_EDGE;
import static com.tv.leanback.BaseGridView.WINDOW_ALIGN_LOW_EDGE;
import static com.tv.leanback.BaseGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED;

/**
 * Maintains Window Alignment information of two axis.
 */
class WindowAlignment {

    /**
     * Maintains alignment information in one direction.
     */
    public static class Axis {
        /**
         * mScrollCenter is used to calculate dynamic transformation based on how far a view
         * is from the mScrollCenter. For example, the views with center close to mScrollCenter
         * will be scaled up.
         */
        private float mScrollCenter;
        /**
         * Right or bottom edge of last child.
         */
        private int mMaxEdge;
        /**
         * Left or top edge of first child, typically should be zero.
         */
        private int mMinEdge;
        /**
         * Max Scroll value
         */
        private int mMaxScroll;
        /**
         * Min Scroll value
         */
        private int mMinScroll;

        private int mWindowAlignment = WINDOW_ALIGN_BOTH_EDGE;

        private int mWindowAlignmentOffset = 0;

        private float mWindowAlignmentOffsetPercent = 50f;

        private int mSize;

        private int mPaddingLow;

        private int mPaddingHigh;

        private boolean mReversedFlow;

        private final String mName; // for debugging

        public Axis(String name) {
            reset();
            mName = name;
        }

        final public int getWindowAlignment() {
            return mWindowAlignment;
        }

        final public void setWindowAlignment(int windowAlignment) {
            mWindowAlignment = windowAlignment;
        }

        final public int getWindowAlignmentOffset() {
            return mWindowAlignmentOffset;
        }

        final public void setWindowAlignmentOffset(int offset) {
            mWindowAlignmentOffset = offset;
        }

        final public void setWindowAlignmentOffsetPercent(float percent) {
            if ((percent < 0 || percent > 100)
                    && percent != WINDOW_ALIGN_OFFSET_PERCENT_DISABLED) {
                throw new IllegalArgumentException();
            }
            mWindowAlignmentOffsetPercent = percent;
        }

        final public float getWindowAlignmentOffsetPercent() {
            return mWindowAlignmentOffsetPercent;
        }

        final public int getScrollCenter() {
            return (int) mScrollCenter;
        }

        /** set minEdge,  Integer.MIN_VALUE means unknown*/
        final public void setMinEdge(int minEdge) {
            mMinEdge = minEdge;
        }

        final public int getMinEdge() {
            return mMinEdge;
        }

        /** set minScroll,  Integer.MIN_VALUE means unknown*/
        final public void setMinScroll(int minScroll) {
            mMinScroll = minScroll;
        }

        final public int getMinScroll() {
            return mMinScroll;
        }

        final public void invalidateScrollMin() {
            mMinEdge = Integer.MIN_VALUE;
            mMinScroll = Integer.MIN_VALUE;
        }

        /** update max edge,  Integer.MAX_VALUE means unknown*/
        final public void setMaxEdge(int maxEdge) {
            mMaxEdge = maxEdge;
        }

        final public int getMaxEdge() {
            return mMaxEdge;
        }

        /** update max scroll,  Integer.MAX_VALUE means unknown*/
        final public void setMaxScroll(int maxScroll) {
            mMaxScroll = maxScroll;
        }

        final public int getMaxScroll() {
            return mMaxScroll;
        }

        final public void invalidateScrollMax() {
            mMaxEdge = Integer.MAX_VALUE;
            mMaxScroll = Integer.MAX_VALUE;
        }

        final public float updateScrollCenter(float scrollTarget) {
            mScrollCenter = scrollTarget;
            return scrollTarget;
        }

        void reset() {
            mScrollCenter = Integer.MIN_VALUE;
            mMinEdge = Integer.MIN_VALUE;
            mMaxEdge = Integer.MAX_VALUE;
        }

        final public boolean isMinUnknown() {
            return mMinEdge == Integer.MIN_VALUE;
        }

        final public boolean isMaxUnknown() {
            return mMaxEdge == Integer.MAX_VALUE;
        }

        final public void setSize(int size) {
            mSize = size;
        }

        final public int getSize() {
            return mSize;
        }

        final public void setPadding(int paddingLow, int paddingHigh) {
            mPaddingLow = paddingLow;
            mPaddingHigh = paddingHigh;
        }

        final public int getPaddingLow() {
            return mPaddingLow;
        }

        final public int getPaddingHigh() {
            return mPaddingHigh;
        }

        final public int getClientSize() {
            return mSize - mPaddingLow - mPaddingHigh;
        }

        final public int getSystemScrollPos(boolean isAtMin, boolean isAtMax) {
            return getSystemScrollPos((int) mScrollCenter, isAtMin, isAtMax);
        }

        final public int getSystemScrollPos(int scrollCenter, boolean isAtMin, boolean isAtMax) {
            int middlePosition;
            if (!mReversedFlow) {
                if (mWindowAlignmentOffset >= 0) {
                    middlePosition = mWindowAlignmentOffset - mPaddingLow;
                } else {
                    middlePosition = mSize + mWindowAlignmentOffset - mPaddingLow;
                }
                if (mWindowAlignmentOffsetPercent != WINDOW_ALIGN_OFFSET_PERCENT_DISABLED) {
                    middlePosition += (int) (mSize * mWindowAlignmentOffsetPercent / 100);
                }
            } else {
                if (mWindowAlignmentOffset >= 0) {
                    middlePosition = mSize - mWindowAlignmentOffset - mPaddingLow;
                } else {
                    middlePosition = - mWindowAlignmentOffset - mPaddingLow;
                }
                if (mWindowAlignmentOffsetPercent != WINDOW_ALIGN_OFFSET_PERCENT_DISABLED) {
                    middlePosition -= (int) (mSize * mWindowAlignmentOffsetPercent / 100);
                }
            }
            int clientSize = getClientSize();
            int afterMiddlePosition = clientSize - middlePosition;
            boolean isMinUnknown = isMinUnknown();
            boolean isMaxUnknown = isMaxUnknown();
            if (!isMinUnknown && !isMaxUnknown
                    && (mWindowAlignment & WINDOW_ALIGN_BOTH_EDGE) == WINDOW_ALIGN_BOTH_EDGE) {
                if (mMaxEdge - mMinEdge <= clientSize) {
                    // total children size is less than view port and we want to align
                    // both edge:  align first child to start edge of view port
                    return mReversedFlow ? mMaxEdge - mPaddingLow - clientSize
                            : mMinEdge - mPaddingLow;
                }
            }
            if (!isMinUnknown) {
                if ((!mReversedFlow ? (mWindowAlignment & WINDOW_ALIGN_LOW_EDGE) != 0
                     : (mWindowAlignment & WINDOW_ALIGN_HIGH_EDGE) != 0)
                        && (isAtMin || scrollCenter - mMinEdge <= middlePosition)) {
                    // scroll center is within half of view port size: align the start edge
                    // of first child to the start edge of view port
                    return mMinEdge - mPaddingLow;
                }
            }
            if (!isMaxUnknown) {
                if ((!mReversedFlow ? (mWindowAlignment & WINDOW_ALIGN_HIGH_EDGE) != 0
                        : (mWindowAlignment & WINDOW_ALIGN_LOW_EDGE) != 0)
                        && (isAtMax || mMaxEdge - scrollCenter <= afterMiddlePosition)) {
                    // scroll center is very close to the end edge of view port : align the
                    // end edge of last children (plus expanded size) to view port's end
                    return mMaxEdge - mPaddingLow - clientSize;
                }
            }
            // else put scroll center in middle of view port
            return scrollCenter - middlePosition - mPaddingLow;
        }

        final public void setReversedFlow(boolean reversedFlow) {
            mReversedFlow = reversedFlow;
        }

        @Override
        public String toString() {
            return "center: " + mScrollCenter + " min:" + mMinEdge + " max:" + mMaxEdge;
        }

    }

    private int mOrientation = HORIZONTAL;

    final public Axis vertical = new Axis("vertical");

    final public Axis horizontal = new Axis("horizontal");

    private Axis mMainAxis = horizontal;

    private Axis mSecondAxis = vertical;

    final public Axis mainAxis() {
        return mMainAxis;
    }

    final public Axis secondAxis() {
        return mSecondAxis;
    }

    final public void setOrientation(int orientation) {
        mOrientation = orientation;
        if (mOrientation == HORIZONTAL) {
            mMainAxis = horizontal;
            mSecondAxis = vertical;
        } else {
            mMainAxis = vertical;
            mSecondAxis = horizontal;
        }
    }

    final public int getOrientation() {
        return mOrientation;
    }

    final public void reset() {
        mainAxis().reset();
    }

    @Override
    public String toString() {
        return new StringBuffer().append("horizontal=")
                .append(horizontal)
                .append("; vertical=")
                .append(vertical)
                .toString();
    }

}
