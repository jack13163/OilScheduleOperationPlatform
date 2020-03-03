package opt.rl4j.fly;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class BeepUtil {
    public static void main(String[] args) {
        playSound("sound/bombo.wav");
    }

    /**
     * 播放音频
     */
    public static void playSound(String filePath) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BeepUtil beep = new BeepUtil();
                    AudioInputStream inputStream = beep.getAudioStream(filePath);
                    beep.play(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private AudioInputStream getAudioStream(String filePath) throws Exception{
        return AudioSystem
                .getAudioInputStream(new BufferedInputStream(new FileInputStream(filePath)));
    }

    private void play(AudioInputStream audioInputStream) throws Exception{
        Clip clip = AudioSystem.getClip();
        AudioListener listener = new AudioListener();
        clip.addLineListener(listener);
        clip.open(audioInputStream);
        try {
            clip.start();
            listener.waitUntilDone();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            clip.close();
        }
        audioInputStream.close();
    }


    class AudioListener implements LineListener {
        private boolean done = false;

        /**
         * This method allows to be notified for each event while playing a
         * sound
         */
        @Override
        public synchronized void update(final LineEvent event) {
            final LineEvent.Type eventType = event.getType();
            if (eventType == LineEvent.Type.STOP || eventType == LineEvent.Type.CLOSE) {
                done = true;
                notifyAll();
            }
        }

        /**
         * This method allows to wait until a sound is completly played
         *
         * @throws InterruptedException
         *             as we work with thread, this exception can occur
         */
        public synchronized void waitUntilDone() throws InterruptedException {
            while (!done)
                wait();
        }
    }
}
