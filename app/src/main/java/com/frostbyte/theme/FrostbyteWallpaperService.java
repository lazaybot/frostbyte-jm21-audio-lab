package com.frostbyte.theme;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public final class FrostbyteWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine() { return new FrostEngine(); }

    private final class FrostEngine extends Engine implements MediaBridge.Listener {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final AudioProbe audio = new AudioProbe();
        private final SimpleDateFormat clock = new SimpleDateFormat("HH:mm", Locale.US);
        private final SimpleDateFormat date = new SimpleDateFormat("EEE // MM.dd.yyyy", Locale.US);
        private final Runnable frame = this::draw;
        private MediaBridge media;
        private boolean visible, lockScreen, audioActive;
        private long startMs;
        private int width, height;
        private final List<String> shortcutPackages = new ArrayList<>();
        private final List<String> shortcutLabels = new ArrayList<>();

        FrostEngine() { setTouchEventsEnabled(true); }

        @Override public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder); startMs = System.currentTimeMillis();
            media = MediaBridge.get(FrostbyteWallpaperService.this); media.addListener(this);
            findMusicApps();
        }
        @Override public void onDestroy() { handler.removeCallbacks(frame); audio.stop(); media.removeListener(this); super.onDestroy(); }
        @Override public void onSurfaceChanged(SurfaceHolder holder, int format, int w, int h) { width = w; height = h; draw(); }
        @Override public void onVisibilityChanged(boolean v) {
            visible = v; handler.removeCallbacks(frame);
            if (v) {
                KeyguardManager km = getSystemService(KeyguardManager.class);
                lockScreen = km != null && km.isKeyguardLocked();
                audioActive = !lockScreen && audio.start(FrostbyteWallpaperService.this, this::requestFrame);
                draw();
            } else { audio.stop(); audioActive = false; }
        }
        @Override public void onMediaChanged() { requestFrame(); }
        private void requestFrame() { if (visible) { handler.removeCallbacks(frame); handler.post(frame); } }

        @Override public void onTouchEvent(MotionEvent e) {
            if (e.getAction() != MotionEvent.ACTION_UP || lockScreen || height == 0) return;
            float y = e.getY() / height, x = e.getX() / width;
            if (y > .70f && y < .78f && !shortcutPackages.isEmpty()) {
                int index = Math.min(shortcutPackages.size() - 1, (int)(x * shortcutPackages.size()));
                Intent launch = getPackageManager().getLaunchIntentForPackage(shortcutPackages.get(index));
                if (launch != null) { launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch); }
            } else if (y > .80f && y < .91f) {
                if (x < .34f) media.previous(); else if (x < .66f) media.toggle(); else media.next();
            }
        }

        private void draw() {
            if (!visible || width <= 0 || height <= 0) return;
            Canvas c = null;
            try { c = getSurfaceHolder().lockCanvas(); if (c != null) render(c); }
            finally { if (c != null) getSurfaceHolder().unlockCanvasAndPost(c); }
            long delay = media.isPlaying() ? 66L : 1000L; // 15 fps while active; clock-only while idle
            handler.removeCallbacks(frame); handler.postDelayed(frame, delay);
        }

        private void render(Canvas c) {
            c.drawColor(ThemeColors.NAVY);
            p.setShader(new LinearGradient(0, 0, 0, height, 0xFF0E1B27, ThemeColors.NAVY, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, width, height, p); p.setShader(null);
            drawScanlines(c); drawHeader(c); drawPlanet(c); drawLandscape(c); drawMedia(c); drawFooter(c);
        }

        private void drawScanlines(Canvas c) {
            p.setColor(0x0B86CBE4); p.setStrokeWidth(1);
            for (int y = 0; y < height; y += Math.max(5, height / 220)) c.drawLine(0, y, width, y, p);
        }
        private void drawHeader(Canvas c) {
            p.setTypeface(android.graphics.Typeface.MONOSPACE); p.setColor(ThemeColors.FROST); p.setTextSize(width * .031f);
            c.drawText("FROSTBYTE // 1984", width * .055f, height * .055f, p);
            p.setTextAlign(Paint.Align.RIGHT); c.drawText(lockScreen ? "LOCK LINK" : "HOME LINK", width * .945f, height * .055f, p); p.setTextAlign(Paint.Align.LEFT);
            p.setColor(ThemeColors.AMBER); p.setTextSize(width * .12f); c.drawText(clock.format(new Date()), width * .05f, height * .14f, p);
            p.setTextSize(width * .03f); p.setColor(ThemeColors.FROST); c.drawText(date.format(new Date()).toUpperCase(Locale.US), width * .055f, height * .17f, p);
        }
        private void drawPlanet(Canvas c) {
            float cx = width * .72f, cy = height * .31f, r = width * .19f;
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(2, width * .003f)); p.setColor(ThemeColors.AMBER);
            c.drawCircle(cx, cy, r, p);
            float angle = ((System.currentTimeMillis() - startMs) % 120000L) / 120000f * 360f;
            c.save(); c.rotate(angle, cx, cy);
            for (int i = -2; i <= 2; i++) c.drawOval(cx-r, cy + i*r*.28f-r*.08f, cx+r, cy + i*r*.28f+r*.08f, p);
            c.drawOval(cx-r*.45f, cy-r, cx+r*.45f, cy+r, p); c.restore();
            p.setColor(0x33FFB547); p.setStyle(Paint.Style.FILL); c.drawCircle(cx, cy, r, p);
        }
        private void drawLandscape(Canvas c) {
            float horizon = height * .49f; p.setStyle(Paint.Style.STROKE); p.setColor(0xAA86CBE4); p.setStrokeWidth(1.2f);
            for (int i = -8; i <= 8; i++) c.drawLine(width*.5f, horizon, width*.5f + i*width*.15f, height*.70f, p);
            for (int i = 0; i < 9; i++) { float t=i/8f; float y=horizon+(height*.21f)*(t*t); c.drawLine(0,y,width,y,p); }
            Path ridge = new Path(); ridge.moveTo(0,horizon); ridge.lineTo(width*.12f,horizon-height*.05f); ridge.lineTo(width*.25f,horizon-height*.015f); ridge.lineTo(width*.39f,horizon-height*.09f); ridge.lineTo(width*.52f,horizon-height*.025f); ridge.lineTo(width*.68f,horizon-height*.07f); ridge.lineTo(width*.84f,horizon-height*.018f); ridge.lineTo(width,horizon-height*.06f); c.drawPath(ridge,p);
        }
        private void drawMedia(Canvas c) {
            float left=width*.05f, top=height*.57f, right=width*.95f, bottom=height*.91f;
            p.setStyle(Paint.Style.FILL); p.setColor(ThemeColors.PANEL); c.drawRect(left,top,right,bottom,p);
            p.setStyle(Paint.Style.STROKE); p.setColor(ThemeColors.DIM_AMBER); p.setStrokeWidth(2); c.drawRect(left,top,right,bottom,p);
            Bitmap art=media.artwork(); float s=width*.22f, ax=left+width*.035f, ay=top+width*.035f;
            if (art != null) { c.drawBitmap(art,null,new Rect((int)ax,(int)ay,(int)(ax+s),(int)(ay+s)),p); }
            else { p.setColor(0xFF101F2B); p.setStyle(Paint.Style.FILL); c.drawRect(ax,ay,ax+s,ay+s,p); p.setStyle(Paint.Style.STROKE); p.setColor(ThemeColors.AMBER); c.drawCircle(ax+s*.5f,ay+s*.5f,s*.31f,p); c.drawCircle(ax+s*.5f,ay+s*.5f,s*.07f,p); }
            float tx=ax+s+width*.035f; p.setStyle(Paint.Style.FILL); p.setTypeface(android.graphics.Typeface.MONOSPACE);
            p.setColor(ThemeColors.AMBER); p.setTextSize(width*.038f); c.drawText(ellipsize(media.title(),22),tx,ay+width*.045f,p);
            p.setColor(ThemeColors.FROST); p.setTextSize(width*.029f); c.drawText(ellipsize(media.artist(),26),tx,ay+width*.09f,p);
            drawSpectrum(c, tx, ay+width*.14f, right-width*.03f, ay+s);
            drawShortcuts(c, left+width*.03f, top+height*.145f, right-width*.03f, top+height*.205f);
            p.setTextAlign(Paint.Align.CENTER); p.setTextSize(width*.058f); p.setColor(ThemeColors.AMBER);
            float by=bottom-height*.045f; c.drawText("|◀",width*.25f,by,p); c.drawText(media.isPlaying()?"Ⅱ":"▶",width*.5f,by,p); c.drawText("▶|",width*.75f,by,p); p.setTextAlign(Paint.Align.LEFT);
        }
        private void drawShortcuts(Canvas c,float l,float t,float r,float b) {
            if (shortcutLabels.isEmpty()) return;
            float gap=width*.012f, cell=(r-l-gap*(shortcutLabels.size()-1))/shortcutLabels.size();
            p.setTextAlign(Paint.Align.CENTER); p.setTextSize(width*.022f); p.setTypeface(android.graphics.Typeface.MONOSPACE);
            for(int i=0;i<shortcutLabels.size();i++) { float x=l+i*(cell+gap); p.setStyle(Paint.Style.STROKE); p.setColor(0x8886CBE4); c.drawRect(x,t,x+cell,b,p); p.setStyle(Paint.Style.FILL); p.setColor(ThemeColors.FROST); c.drawText(ellipsize(shortcutLabels.get(i).toUpperCase(Locale.US),11),x+cell*.5f,t+(b-t)*.62f,p); }
            p.setTextAlign(Paint.Align.LEFT);
        }
        private void drawSpectrum(Canvas c,float l,float t,float r,float b) {
            byte[] fft=audio.fft(); int bars=18; float gap=3, bw=(r-l-(bars-1)*gap)/bars; p.setStyle(Paint.Style.FILL); p.setColor(0xBB86CBE4);
            for(int i=0;i<bars;i++) { float norm=.08f; if(audioActive&&fft!=null&&fft.length>i*2+3){int re=fft[i*2+2],im=fft[i*2+3];norm=Math.min(1f,(float)Math.hypot(re,im)/96f);} else if(media.isPlaying()){norm=.18f+.12f*(float)Math.sin(i*.8+System.currentTimeMillis()/350.0);} float h=Math.max(2,(b-t)*norm); c.drawRect(l+i*(bw+gap),b-h,l+i*(bw+gap)+bw,b,p); }
        }
        private void drawFooter(Canvas c) {
            BatteryManager bm=(BatteryManager)getSystemService(Context.BATTERY_SERVICE); int pct=bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            p.setColor(ThemeColors.FROST); p.setTextSize(width*.025f); p.setTypeface(android.graphics.Typeface.MONOSPACE);
            c.drawText("PWR "+pct+"%   FFT "+(audioActive?"LIVE":"SAFE")+"   MEM ECO",width*.055f,height*.96f,p);
        }
        private String ellipsize(String s,int max){return s.length()<=max?s:s.substring(0,max-1)+"…";}
        private void findMusicApps() {
            String[] candidates={"com.fiio.music","com.spotify.music","com.apple.android.music","com.aspiro.tidal","com.qobuz.music","com.amazon.mp3"};
            for(String pkg:candidates) {
                Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);
                if(launch!=null) { shortcutPackages.add(pkg); try { shortcutLabels.add(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString()); } catch(Exception e) { shortcutLabels.add("MUSIC"); } if(shortcutPackages.size()==3) break; }
            }
        }
    }
}
