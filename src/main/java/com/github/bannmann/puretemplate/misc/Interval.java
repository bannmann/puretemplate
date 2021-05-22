package com.github.bannmann.puretemplate.misc;

/** An inclusive interval {@code a..b}.  Used to track ranges in output and
 *  template patterns (for debugging).
 */
public class Interval {
    public int a;
    public int b;
    public Interval(int a, int b) { this.a=a; this.b=b; }
    @Override
    public String toString() { return a+".."+b; }    
}
