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

相较于刚才的dispatchTouchEvent方法，onTouchEvent方法复杂了很多，不过没关系，我们只挑重点看就可以了。<br>
首先在第14行我们可以看出，如果该控件是可以点击的就会进入到第16行的switch判断中去，而如果当前的事件是抬起手指，
则会进入到MotionEvent.ACTION_UP这个case当中。在经过种种判断之后，会执行到第38行的performClick()方法，那我们进入到这个方法里瞧一瞧：<br>

    public boolean performClick() {  
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);  
        if (mOnClickListener != null) {  
            playSoundEffect(SoundEffectConstants.CLICK);  
            mOnClickListener.onClick(this);  
            return true;  
        }  
        return false;  
    }  

可以看到，只要mOnClickListener不是null，就会去调用它的onClick方法，那mOnClickListener又是在哪里赋值的呢？经过寻找后找到如下方法：<br>

    public void setOnClickListener(OnClickListener l) {  
        if (!isClickable()) {  
            setClickable(true);  
        }  
        mOnClickListener = l;  
    }  
    
一切都是那么清楚了！当我们通过调用setOnClickListener方法来给控件注册一个点击事件时，就会给mOnClickListener赋值。
然后每当控件被点击时，都会在performClick()方法里回调被点击控件的onClick方法。<br />

这样View的整个事件分发的流程就让我们搞清楚了！不过别高兴的太早，现在还没结束，还有一个很重要的知识点需要说明，就是touch事件的层级传递。
我们都知道如果给一个控件注册了touch事件，每次点击它的时候都会触发一系列的ACTION_DOWN，ACTION_MOVE，ACTION_UP等事件。
这里需要注意，如果你在执行ACTION_DOWN的时候返回了false，后面一系列其它的action就不会再得到执行了。
简单的说，就是当dispatchTouchEvent在进行事件分发的时候，只有前一个action返回true，才会触发后一个action。<br>
说到这里，很多的朋友肯定要有巨大的疑问了。这不是在自相矛盾吗？前面的例子中，明明在onTouch事件里面返回了false，ACTION_DOWN和ACTION_UP不是都得到执行了吗？
其实你只是被假象所迷惑了，让我们仔细分析一下，在前面的例子当中，我们到底返回的是什么。
参考着我们前面分析的源码，首先在onTouch事件里返回了false，就一定会进入到onTouchEvent方法中，然后我们来看一下onTouchEvent方法的细节。
由于我们点击了按钮，就会进入到第14行这个if判断的内部，然后你会发现，不管当前的action是什么，最终都一定会走到第89行，返回一个true。<br>
明明在onTouch事件里返回了false，系统还是在onTouchEvent方法中帮你返回了true。就因为这个原因，才使得前面的例子中ACTION_UP可以得到执行。
那我们可以换一个控件，将按钮替换成ImageView，然后给它也注册一个touch事件，并返回false。<br>
实例:ActivityA.onDown(ImageView)
运行一下程序，点击ImageView，你会发现结果如下：<br>
<image src="./image/image.png"/><br>
在ACTION_DOWN执行完后，后面的一系列action都不会得到执行了。这又是为什么呢？因为ImageView和按钮不同，它是默认不可点击的，
因此在onTouchEvent的第14行判断时无法进入到if的内部，直接跳到第91行返回了false，也就导致后面其它的action都无法执行了。<br>


=============================分割线====================================

接下来介绍一下 ViewGroup 里面的事件传递.
实例:ActivityB.setLisener()<br>
<image src="./image/jietu.png"/><br>
分别点击一下btn1、btn2、btn3和空白区域，打印结果如下所示：<br>
<image src="./image/my.png"/><br>
当点击按钮的时候，MyLayout注册的onTouch方法并不会执行，只有点击空白区域的时候才会执行该方法。
你可以先理解成Button的onClick方法将事件消费掉了，因此事件不会再继续向下传递。<br>

那就说明Android中的touch事件是先传递到View，再传递到ViewGroup的？现在下结论还未免过早了，让我们再来做一个实验。
查阅文档可以看到，ViewGroup中有一个onInterceptTouchEvent方法，我们来看一下这个方法的源码：

    /** 
     * Implement this method to intercept all touch screen motion events.  This 
     * allows you to watch events as they are dispatched to your children, and 
     * take ownership of the current gesture at any point. 
     * 
     * <p>Using this function takes some care, as it has a fairly complicated 
     * interaction with {@link View#onTouchEvent(MotionEvent) 
     * View.onTouchEvent(MotionEvent)}, and using it requires implementing 
     * that method as well as this one in the correct way.  Events will be 
     * received in the following order: 
     * 
     * <ol> 
     * <li> You will receive the down event here. 
     * <li> The down event will be handled either by a child of this view 
     * group, or given to your own onTouchEvent() method to handle; this means 
     * you should implement onTouchEvent() to return true, so you will 
     * continue to see the rest of the gesture (instead of looking for 
     * a parent view to handle it).  Also, by returning true from 
     * onTouchEvent(), you will not receive any following 
     * events in onInterceptTouchEvent() and all touch processing must 
     * happen in onTouchEvent() like normal. 
     * <li> For as long as you return false from this function, each following 
     * event (up to and including the final up) will be delivered first here 
     * and then to the target's onTouchEvent(). 
     * <li> If you return true from here, you will not receive any 
     * following events: the target view will receive the same event but 
     * with the action {@link MotionEvent#ACTION_CANCEL}, and all further 
     * events will be delivered to your onTouchEvent() method and no longer 
     * appear here. 
     * </ol> 
     * 
     * @param ev The motion event being dispatched down the hierarchy. 
     * @return Return true to steal motion events from the children and have 
     * them dispatched to this ViewGroup through onTouchEvent(). 
     * The current target will receive an ACTION_CANCEL event, and no further 
     * messages will be delivered here. 
     */  
    public boolean onInterceptTouchEvent(MotionEvent ev) {  
        return false;  
    } 
    
如果不看源码你还真可能被这注释吓到了，这么长的英文注释看得头都大了。可是源码竟然如此简单！只有一行代码，返回了一个false！
好吧，既然是布尔型的返回，那么只有两种可能，我们在MyLayout中重写这个方法，然后返回一个true试试，代码如下所示：<br>
实例:MyViewGroup<br>
分别点击一下btn1、btn2、btn3和空白区域，打印结果如下所示：<br>
<image src="./image/onInterceptTouchEvent.png"/><br>







