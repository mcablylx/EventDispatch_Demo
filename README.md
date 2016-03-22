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
可以看到，onTouch是优先于onClick执行的，并且onTouch执行了两次，一次是ACTION_DOWN，一次是ACTION_UP(你还可能会有多次ACTION_MOVE的执行，如果你手抖了一下)。因此事件传递的顺序是先经过onTouch，再传递到onClick。<br>
细心的朋友应该可以注意到，onTouch方法是有返回值的，这里我们返回的是false，如果我们尝试把onTouch方法里的返回值改成true，再运行一次，结果如下：<br>
<image src="./image/onTouch1.png"/><br>
onClick方法不再执行了！为什么会这样呢？你可以先理解成onTouch方法返回true就认为这个事件被onTouch消费掉了，因而不会再继续向下传递。<br>

出现上述现象的原理是什么?<br>
首先你需要知道一点，只要你触摸到了任何一个控件，就一定会调用该控件的dispatchTouchEvent方法。那当我们去点击按钮的时候，就会去调用Button类里的dispatchTouchEvent方法，可是你会发现Button类里并没有这个方法，那么就到它的父类TextView里去找一找，你会发现TextView里也没有这个方法，那没办法了，只好继续在TextView的父类View里找一找，这个时候你终于在View里找到了这个方法，示意图如下<br>
<image src="./image/dispatchTouchEvent.png"/><br>
然后我们来看一下View中dispatchTouchEvent方法的源码：<br>

public boolean dispatchTouchEvent(MotionEvent event) {  
    if (mOnTouchListener != null && (mViewFlags & ENABLED_MASK) == ENABLED &&  
            mOnTouchListener.onTouch(this, event)) {  
        return true;  
    }  
    return onTouchEvent(event);  
} 






