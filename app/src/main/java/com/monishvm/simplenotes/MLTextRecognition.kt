package com.monishvm.simplenotes

import android.graphics.Bitmap
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer

class MLTextRecognition {

    companion object {

        private lateinit var analyzer: MLTextAnalyzer

        fun detectImage(src: Bitmap): Task<MLText> {
            setupAnalyzerForDetection()

            val frame = MLFrame.fromBitmap(src)
            val task = analyzer.asyncAnalyseFrame(frame)

            stopAnalyzer()
            return task
        }

        private fun setupAnalyzerForDetection() {
            val setting = MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                .setLanguage("en")
                .create()
            analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
        }


        private fun stopAnalyzer() {
            if (analyzer.isAvailable) {
                analyzer.stop();
            }
        }

    }
}
