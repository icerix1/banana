package com.bananalab.lite;
/** Editable animation entry points connected through ProjectRuntime. */
final class AnimationProfile {
 static float jump(float t){ return -12f*(float)Math.sin(t*Math.PI); }
 static float sway(float t){ return 16f*(float)Math.sin(t*Math.PI*2); }
}
