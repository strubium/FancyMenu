package de.keksuccino.fancymenu.util.resources.audio.ogg.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;
import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.Objects;

public class OggAudioBuffer {

    private ByteBuffer dataBuffer;
    private final AudioFormat audioFormat;
    private boolean bufferPrepared;
    private int source;

    public OggAudioBuffer(@NotNull ByteBuffer dataBuffer, @NotNull AudioFormat audioFormat) {
        this.dataBuffer = Objects.requireNonNull(dataBuffer);
        this.audioFormat = Objects.requireNonNull(audioFormat);
    }

    @Nullable
    public Integer getSource() {
        return this.prepare() ? this.source : null;
    }

    public boolean prepare() {
        if (!this.bufferPrepared) {
            this.bufferPrepared = true;
            int audioFormat = OpenALUtils.getAudioFormatAsOpenAL(this.audioFormat);
            int[] bufferSource = new int[1];
            AL10.alGenBuffers(bufferSource);
            if (OpenALUtils.checkAndPrintOpenAlError("Generate OpenAL buffer")) return false;
            AL10.alBufferData(bufferSource[0], audioFormat, this.dataBuffer, (int)this.audioFormat.getSampleRate());
            if (OpenALUtils.checkAndPrintOpenAlError("Set OpenAL buffer data")) return false;
            this.source = bufferSource[0];
            this.dataBuffer = null;
        }
        return true;
    }

    public void delete() {
        if (this.isLoadedInOpenAL()) {
            AL10.alDeleteBuffers(new int[]{this.source});
            OpenALUtils.checkAndPrintOpenAlError("Delete OpenAL buffers");
        }
        this.bufferPrepared = false;
    }

    public boolean isLoadedInOpenAL() {
        return this.bufferPrepared && AL10.alIsBuffer(this.source);
    }

}
