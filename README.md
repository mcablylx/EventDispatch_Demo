# EventDispatch_Demo

Android事件分发机制解析.

在网上读了好几篇关于Android事件分发机制的文章.有写的很不错的.郭大神写的是尤为出色.<br>
两篇文章:
 - [Android事件分发机制完全解析，带你从源码的角度彻底理解(上)](http://blog.csdn.net/guolin_blog/article/details/9097463)<br>
 - [Android事件分发机制完全解析，带你从源码的角度彻底理解(下)](http://blog.csdn.net/guolin_blog/article/details/9153747)

根据郭大神的博客. 把栗子添里面. 方便观看.

首先,我们给一个Button注册一个onClickListener  和  onTouchListener(示例:ActivityA)<br>
运行后结果如下<br>
<image src="./image/onClick_onTouch.png"/><br>
可以看到，onTouch是优先于onClick执行的，并且onTouch执行了两次，一次是ACTION_DOWN，一次是ACTION_UP(你还可能会有多次ACTION_MOVE的执行，如果你手抖了一下)。
因此事件传递的顺序是先经过onTouch，再传递到onClick。<br>
细心的朋友应该可以注意到，onTouch方法是有返回值的，这里我们返回的是false，如果我们尝试把onTouch方法里的返回值改成true，再运行一次，结果如下：<br>
<image src="./image/onTouch1.png"/><br>
onClick方法不再执行了！为什么会这样呢？你可以先理解成onTouch方法返回true就认为这个事件被onTouch消费掉了，因而不会再继续向下传递。<br>

出现上述现象的原理是什么?<br>
首先你需要知道一点，只要你触摸到了任何一个控件，就一定会调用该控件的dispatchTouchEvent方法。那当我们去点击按钮的时候，
就会去调用Button类里的dispatchTouchEvent方法，可是你会发现Button类里并没有这个方法，那么就到它的父类TextView里去找一找，
你会发现TextView里也没有这个方法，那没办法了，只好继续在TextView的父类View里找一找，这个时候你终于在View里找到了这个方法，示意图如下<br>
<image src="./image/dispatchTouchEvent.png"/><br>
然后我们来看一下View中dispatchTouchEvent方法的源码：<br>
 
    public boolean dispatchTouchEvent(MotionEvent event) {  
        if (mOnTouchListener != null && (mViewFlags & ENABLED_MASK) == ENABLED &&  
                mOnTouchListener.onTouch(this, event)) {  
            return true;  
        }  
        return onTouchEvent(event);  
    } 

如果mOnTouchListener != null，(mViewFlags & ENABLED_MASK) == ENABLED和mOnTouchListener.onTouch(this, event)这三个条件都为真，
就返回true，否则就去执行onTouchEvent(event)方法并返回。<br>

    public void setOnTouchListener(OnTouchListener l) {  
        mOnTouchListener = l;  
    } 

mOnTouchListener在setOnTouchListener时候被赋值.<br>

第二个条件(mViewFlags & ENABLED_MASK) == ENABLED是判断当前点击的控件是否是enable的，按钮默认都是enable的，因此这个条件恒定为true。<br>

第三个条件就比较关键了，mOnTouchListener.onTouch(this, event)，其实也就是去回调控件注册touch事件时的onTouch方法。
也就是说如果我们在onTouch方法里返回true，就会让这三个条件全部成立，从而整个方法直接返回true。如果我们在onTouch方法里返回false，就会再去执行onTouchEvent(event)方法。<br />

现在我们可以结合前面的例子来分析一下了，首先在dispatchTouchEvent中最先执行的就是onTouch方法，因此onTouch肯定是要优先于onClick执行的，也是印证了刚刚的打印结果。
而如果在onTouch方法里返回了true，就会让dispatchTouchEvent方法直接返回true，不会再继续往下执行。而打印结果也证实了如果onTouch返回true，onClick就不会再执行了。<br>

根据以上源码的分析，从原理上解释了我们前面例子的运行结果。而上面的分析还透漏出了一个重要的信息，
那就是onClick的调用肯定是在onTouchEvent(event)方法中的！那我们马上来看下onTouchEvent的源码，如下所示：<br>

    public boolean onTouchEvent(MotionEvent event) {  
        final int viewFlags = mViewFlags;  
        if ((viewFlags & ENABLED_MASK) == DISABLED) {  
            // A disabled view that is clickable still consumes the touch  
            // events, it just doesn't respond to them.  
            return (((viewFlags & CLICKABLE) == CLICKABLE ||  
                    (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE));  
        }  
        if (mTouchDelegate != null) {  
            if (mTouchDelegate.onTouchEvent(event)) {  
                return true;  
            }  
        }  
        if (((viewFlags & CLICKABLE) == CLICKABLE ||  
                (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)) {  
            switch (event.getAction()) {  
                case MotionEvent.ACTION_UP:  
                    boolean prepressed = (mPrivateFlags & PREPRESSED) != 0;  
                    if ((mPrivateFlags & PRESSED) != 0 || prepressed) {  
                        // take focus if we don't have it already and we should in  
                        // touch mode.  
                        boolean focusTaken = false;  
                        if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {  
                            focusTaken = requestFocus();  
                        }  
                        if (!mHasPerformedLongPress) {  
                            // This is a tap, so remove the longpress check  
                            removeLongPressCallback();  
                            // Only perform take click actions if we were in the pressed state  
                            if (!focusTaken) {  
                                // Use a Runnable and post this rather than calling  
                                // performClick directly. This lets other visual state  
                                // of the view update before click actions start.  
                                if (mPerformClick == null) {  
                                    mPerformClick = new PerformClick();  
                                }  
                                if (!post(mPerformClick)) {  
                                    performClick();  
                                }  
                            }  
                        }  
                        if (mUnsetPressedState == null) {  
                            mUnsetPressedState = new UnsetPressedState();  
                        }  
                        if (prepressed) {  
                            mPrivateFlags |= PRESSED;  
                            refreshDrawableState();  
                            postDelayed(mUnsetPressedState,  
                                    ViewConfiguration.getPressedStateDuration());  
                        } else if (!post(mUnsetPressedState)) {  
                            // If the post failed, unpress right now  
                            mUnsetPressedState.run();  
                        }  
                        removeTapCallback();  
                    }  
                    break;  
                case MotionEvent.ACTION_DOWN:  
                    if (mPendingCheckForTap == null) {  
                        mPendingCheckForTap = new CheckForTap();  
                    }  
                    mPrivateFlags |= PREPRESSED;  
                    mHasPerformedLongPress = false;  
                    postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());  
                    break;  
                case MotionEvent.ACTION_CANCEL:  
                    mPrivateFlags &= ~PRESSED;  
                    refreshDrawableState();  
                    removeTapCallback();  
                    break;  
                case MotionEvent.ACTION_MOVE:  
                    final int x = (int) event.getX();  
                    final int y = (int) event.getY();  
                    // Be lenient about moving outside of buttons  
                    int slop = mTouchSlop;  
                    if ((x < 0 - slop) || (x >= getWidth() + slop) ||  
                            (y < 0 - slop) || (y >= getHeight() + slop)) {  
                        // Outside button  
                        removeTapCallback();  
                        if ((mPrivateFlags & PRESSED) != 0) {  
                            // Remove any future long press/tap checks  
                            removeLongPressCallback();  
                            // Need to switch from pressed to not pressed  
                            mPrivateFlags &= ~PRESSED;  
                            refreshDrawableState();  
                        }  
                    }  
                    break;  
            }  
            return true;  
        }  
        return false;  
    } 

















