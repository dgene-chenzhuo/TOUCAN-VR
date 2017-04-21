/*
 * Copyright 2017 Laboratoire I3S, CNRS, Université côte d'azur
 *
 * Author: Romaric Pighetti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.unice.i3s.uca4svr.toucan_vr.tracking;

import android.os.Environment;
import android.util.Log;

import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.SystemClock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

/**
 * Tracks the bandwidth consumed during the video playback.
 */
public class BandwidthConsumedTracker implements TransferListener<Object> {

    // Each logger must have a different ID,
    // so that creating a new logger won't override the previous one
    private static int loggerNextID = 0;

    private final Logger logger;

    private long totalBytesConsumed = 0;
    private boolean firstTransfer = true;
    private final Clock clock;

    /**
     * Initialize a {@link BandwidthConsumedTracker}, that will record the consumed
     * bandwidth during playback to a file name logFilePrefix_date.csv.
     * Be aware that tracking is done by calling the <code>track</code> method every time
     * and entry is needed.
     *
     * @param logFilePrefix The prefix for the log file name
     */
    public BandwidthConsumedTracker(String logFilePrefix) {

        clock = new SystemClock();

        String logFilePath = Environment.getExternalStoragePublicDirectory("toucan/logs/")
                + File.separator
                + createLogFileName(logFilePrefix);
        Log.d("BandwidthTracker", logFilePath);
        // logFilePath = context.getContext().getFileStreamPath(logFilePath).getAbsolutePath();

        // Initialize and configure a new logger in logback
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%msg%n");
        encoder1.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(lc);
        fileAppender.setFile(logFilePath);
        fileAppender.setEncoder(encoder1);
        fileAppender.start();

        // getting the instanceof the logger
        logger = LoggerFactory.getLogger("fr.unice.i3s.uca4svr.tracking.BandwidthConsumedTracker"
                + loggerNextID++);
        // I know the logger is from logback, this is the implementation i'm using below slf4j API.
        ((ch.qos.logback.classic.Logger) logger).addAppender(fileAppender);
    }

    /**
     * Builds the name of the logfile by appending the date to the logFilePrefix
     * @param logFilePrefix the prefix for the log file
     * @return the name of the log file
     */
    private String createLogFileName(String logFilePrefix) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date date = new Date();
        return String.format("%s_bandwidth_%s.csv", logFilePrefix, dateFormat.format(date));
    }

    @Override
    public void onTransferStart(Object source, DataSpec dataSpec) {
        if (firstTransfer) {
            logger.error(String.format(Locale.ENGLISH, "%d, %d",
                    clock.elapsedRealtime(), totalBytesConsumed));
            firstTransfer = false;
        }
    }

    @Override
    public void onBytesTransferred(Object source, int bytesTransferred) {
        totalBytesConsumed += bytesTransferred;
        logger.error(String.format(Locale.ENGLISH, "%d, %d",
                clock.elapsedRealtime(), totalBytesConsumed));
    }

    @Override
    public void onTransferEnd(Object source) {

    }
}