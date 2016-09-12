package com.towery.screenrecordball.thread

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by User on 2016/9/10.
 */
class ScreenRecorder(width: Int, height: Int, bitrate: Int, dpi: Int, mp: MediaProjection?, dstPath: String) : Thread() {
    private val TAG = "ScreenRecorder"

    private var mWidth: Int = 0
    private var mHeight: Int =0
    private var mBitRate: Int =0
    private var mDpi: Int =0
    private var mDstPath: String = ""
    private var mMediaProjection: MediaProjection? = null
    // parameters for the encoder
    private val MIME_TYPE = "video/avc" // H.264 Advanced Video Coding
    private val FRAME_RATE = 30 // 30 fps
    private val IFRAME_INTERVAL = 10 // 10 seconds between I-frames
    private val TIMEOUT_US = 10000

    private var mEncoder: MediaCodec? = null
    private var mSurface: Surface? = null
    private var mMuxer: MediaMuxer? = null
    private var mMuxerStarted = false
    private var mVideoTrackIndex = -1
    private val mQuit = AtomicBoolean(false)
    private val mBufferInfo = MediaCodec.BufferInfo()
    private var mVirtualDisplay: VirtualDisplay? = null

    init{
        mWidth = width
        mHeight = height
        mBitRate = bitrate
        mDpi = dpi
        mMediaProjection = mp
        mDstPath = dstPath
    }

    /**
     * stop task
     */
    fun quit() {
        mQuit.set(true)
    }

     override fun run() {
        try {
            try {
                prepareEncoder()
                mMuxer = MediaMuxer(mDstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            mVirtualDisplay = mMediaProjection!!.createVirtualDisplay(TAG + "-display",
                    mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurface, null, null)
            Log.d(TAG, "created virtual display: " + mVirtualDisplay!!)
            recordVirtualDisplay()

        } finally {
            release()
        }
    }

    private fun recordVirtualDisplay() {
        while (!mQuit.get()) {
            val index = mEncoder!!.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US.toLong())
            Log.i(TAG, "dequeue output buffer index=" + index)
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat()

            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "retrieving buffers time out!")
                try {
                    // wait 10ms
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                }

            } else if (index >= 0) {

                if (!mMuxerStarted) {
                    throw IllegalStateException("MediaMuxer dose not call addTrack(format) ")
                }
                encodeToVideoTrack(index)

                mEncoder!!.releaseOutputBuffer(index, false)
            }
        }
    }

    private fun encodeToVideoTrack(index: Int) {
        var encodedData = mEncoder!!.getOutputBuffer(index)

        if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG !== 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.
            // Ignore it.
            Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG")
            mBufferInfo.size = 0
        }
        if (mBufferInfo.size === 0) {
            Log.d(TAG, "info.size == 0, drop it.")
            encodedData = null
        } else {
            Log.d(TAG, "got buffer, info: size=" + mBufferInfo.size
                    + ", presentationTimeUs=" + mBufferInfo.presentationTimeUs
                    + ", offset=" + mBufferInfo.offset)
        }
        if (encodedData != null) {
            encodedData!!.position(mBufferInfo.offset)
            encodedData!!.limit(mBufferInfo.offset + mBufferInfo.size)
            mMuxer!!.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo)
            Log.i(TAG, "sent " + mBufferInfo.size + " bytes to muxer...")
        }
    }

    private fun resetOutputFormat() {
        // should happen before receiving buffers, and should only happen once
        if (mMuxerStarted) {
            throw IllegalStateException("output format already changed!")
        }
        val newFormat = mEncoder!!.getOutputFormat()

        Log.i(TAG, "output format changed.\n new format: " + newFormat.toString())
        mVideoTrackIndex = mMuxer!!.addTrack(newFormat)
        mMuxer!!.start()
        mMuxerStarted = true
        Log.i(TAG, "started media muxer, videoIndex=" + mVideoTrackIndex)
    }

    @Throws(IOException::class)
    private fun prepareEncoder() {

        val format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL)

        Log.d(TAG, "created video format: " + format)
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE)
        mEncoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mSurface = mEncoder!!.createInputSurface()
        Log.d(TAG, "created input surface: " + mSurface!!)
        mEncoder!!.start()
    }

    private fun release() {
        if (mEncoder != null) {
            mEncoder!!.stop()
            mEncoder!!.release()
            mEncoder = null
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay!!.release()
        }
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
        }
        if (mMuxer != null) {
            mMuxer!!.stop()
            mMuxer!!.release()
            mMuxer = null
        }
    }
}