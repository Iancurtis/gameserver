package com.znl.event.impl.Friend;

import com.znl.event.annotation.ZeroTimeAnnotation;
import com.znl.event.base.BaseZeroEvent;

/**
 * Created by Administrator on 2016/6/16.
 */
@ZeroTimeAnnotation
public class ZeroEvent implements BaseZeroEvent {
    @Override
    public void zeroHandler() {
        System.err.println("friend has success do zero event!!!");
    }
}
