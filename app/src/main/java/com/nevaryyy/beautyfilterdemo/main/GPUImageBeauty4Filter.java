package com.nevaryyy.beautyfilterdemo.main;

import android.opengl.GLES20;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * @author wangshuo
 */
public class GPUImageBeauty4Filter extends GPUImageFilter {
    public static final String BILATERAL_FRAGMENT_SHADER = "" +
            "precision highp float;\n"+
                "uniform sampler2D inputImageTexture;\n"+
                "uniform vec2 singleStepOffset;\n"+
                "uniform int iternum;\n"+
                "uniform float aaCoef;\n"+
                "uniform float mixCoef;\n"+
                "varying highp vec2 textureCoordinate;\n"+
                "const float distanceNormalizationFactor = 4.0;\n"+
                "const mat3 saturateMatrix = mat3(1.1102,-0.0598,-0.061,-0.0774,1.0826,-0.1186,-0.0228,-0.0228,1.1772);\n"+
                "void main() {\n"+
                "    vec2 blurCoord1s[14];\n"+
                "    blurCoord1s[0] = textureCoordinate + singleStepOffset * vec2( 0.0, -10.0);\n"+
                "    blurCoord1s[1] = textureCoordinate + singleStepOffset * vec2( 8.0, -5.0);\n"+
                "    blurCoord1s[2] = textureCoordinate + singleStepOffset * vec2( 8.0, 5.0);\n"+
                "    blurCoord1s[3] = textureCoordinate + singleStepOffset * vec2( 0.0, 10.0);\n"+
                "    blurCoord1s[4] = textureCoordinate + singleStepOffset * vec2( -8.0, 5.0);\n"+
                "    blurCoord1s[5] = textureCoordinate + singleStepOffset * vec2( -8.0, -5.0);\n"+
                "    blurCoord1s[6] = textureCoordinate + singleStepOffset * vec2( 0.0, -6.0);\n"+
                "    blurCoord1s[7] = textureCoordinate + singleStepOffset * vec2( -4.0, -4.0);\n"+
                "    blurCoord1s[8] = textureCoordinate + singleStepOffset * vec2( -6.0, 0.0);\n"+
                "    blurCoord1s[9] = textureCoordinate + singleStepOffset * vec2( -4.0, 4.0);\n"+
                "    blurCoord1s[10] = textureCoordinate + singleStepOffset * vec2( 0.0, 6.0);\n"+
                "    blurCoord1s[11] = textureCoordinate + singleStepOffset * vec2( 4.0, 4.0);\n"+
                "    blurCoord1s[12] = textureCoordinate + singleStepOffset * vec2( 6.0, 0.0);\n"+
                "    blurCoord1s[13] = textureCoordinate + singleStepOffset * vec2( 4.0, -4.0);\n"+
                "    vec3 centralColor;\n"+
                "    float central;\n"+
                "    float gaussianWeightTotal;\n"+
                "    float sum;\n"+
                "    float sampleColor;\n"+
                "    float distanceFromCentralColor;\n"+
                "    float gaussianWeight;\n"+
                "    central = texture2D( inputImageTexture, textureCoordinate ).g;\n"+
                "    gaussianWeightTotal = 0.2;\n"+
                "    sum = central * 0.2;\n"+
                "    for (int i = 0; i < 6; i++) {\n"+
                "       sampleColor = texture2D( inputImageTexture, blurCoord1s[i] ).g;\n"+
                "        distanceFromCentralColor = min( abs( central - sampleColor ) * distanceNormalizationFactor, 1.0 );\n"+
                "        gaussianWeight = 0.05 * (1.0 - distanceFromCentralColor);\n"+
                "        gaussianWeightTotal += gaussianWeight;\n"+
                "        sum += sampleColor * gaussianWeight;\n"+
                "    }\n"+
                "    for (int i = 6; i < 14; i++) {\n"+
                "       sampleColor = texture2D( inputImageTexture, blurCoord1s[i] ).g;\n"+
                "       distanceFromCentralColor = min( abs( central - sampleColor ) * distanceNormalizationFactor, 1.0 );\n"+
                "       gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n"+
                "       gaussianWeightTotal += gaussianWeight;\n"+
                "       sum += sampleColor * gaussianWeight;\n"+
                "    }\n"+
                "    sum = sum / gaussianWeightTotal;\n"+
                "    centralColor = texture2D( inputImageTexture, textureCoordinate ).rgb;\n"+
                "    sampleColor = centralColor.g - sum + 0.5;\n"+
                "    for (int i = 0; i < iternum; ++i) {\n"+
                "        if (sampleColor <= 0.5) {\n"+
                "            sampleColor = sampleColor * sampleColor * 2.0;\n"+
                "        }else {\n"+
                "            sampleColor = 1.0 - ((1.0 - sampleColor)*(1.0 - sampleColor) * 2.0);\n"+
                "        }\n"+
                "    }\n"+
                "    float aa = 1.0 + pow( centralColor.g, 0.3 )*aaCoef;\n"+
                "    vec3 smoothColor = centralColor*aa - vec3( sampleColor )*(aa - 1.0);\n"+
                "    smoothColor = clamp( smoothColor, vec3( 0.0 ), vec3( 1.0 ) );\n"+
                "    smoothColor = mix( centralColor, smoothColor, pow( centralColor.g, 0.33 ) );\n"+
                "    smoothColor = mix( centralColor, smoothColor, pow( centralColor.g, mixCoef ) );\n"+
                "    gl_FragColor = vec4( pow( smoothColor, vec3( 0.96 ) ), 1.0 );\n"+
                "    vec3 satcolor = gl_FragColor.rgb * saturateMatrix;\n"+
                "    gl_FragColor.rgb = mix( gl_FragColor.rgb, satcolor, 0.23 );\n"+
                "}";

    private float beautyLevel;
    private float brightLevel;
    private int toneLevel;

    private int singleStepOffsetLocation;
    private int beautyLocation;
    private int brightLocation;
    private int toneLocation;


    public GPUImageBeauty4Filter() {
        super(NO_FILTER_VERTEX_SHADER, BILATERAL_FRAGMENT_SHADER);
        beautyLevel = 60;
        brightLevel = 60;
        toneLevel = 60;
    }

    @Override
    public void onInit() {
        super.onInit();
        this.singleStepOffsetLocation = GLES20.glGetUniformLocation(this.getProgram(), "singleStepOffset");
        this.beautyLocation = GLES20.glGetUniformLocation(this.getProgram(), "aaCoef");
        this.brightLocation = GLES20.glGetUniformLocation(this.getProgram(), "mixCoef");
        this.toneLocation = GLES20.glGetUniformLocation(this.getProgram(), "iternum");

        onDrawArraysPre();
    }

    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        float var10001 = (float)width;
        float var5 = (float)height;
        float var4 = var10001;
        this.setFloatVec2(this.singleStepOffsetLocation, new float[]{2.0F / var4, 2.0F / var5});
    }

    public void setFilterLevel(float level1, float level2, float level3) {
        this.setFilterLevel(level1);
    }

    public void setFilterLevel(float flag) {
        switch((int)(flag / 20.0F + 1.0F)) {
            case 1:
                this.setFilterLevel(1, 0.19F, 0.54F);
                return;
            case 2:
                this.setFilterLevel(2, 0.29F, 0.54F);
                return;
            case 3:
                this.setFilterLevel(3, 0.17F, 0.39F);
                return;
            case 4:
                this.setFilterLevel(3, 0.25F, 0.54F);
                return;
            case 5:
                this.setFilterLevel(4, 0.13F, 0.54F);
                return;
            case 6:
                this.setFilterLevel(4, 0.19F, 0.69F);
                return;
            default:
                this.setFilterLevel(0, 0.0F, 0.0F);
        }
    }

    private void setFilterLevel(int var1, float var2, float var3) {
        this.toneLevel = var1;
        this.beautyLevel = var2;
        this.brightLevel = var3;
        onDrawArraysPre();
    }

    protected void onDrawArraysPre() {
        /*GLES20.glUniform1i(this.toneLocation, this.toneLevel);
        GLES20.glUniform1f(this.beautyLocation, this.beautyLevel);
        GLES20.glUniform1f(this.brightLocation, this.brightLevel);*/
        setInteger(this.toneLocation, this.toneLevel);
        setFloat(this.beautyLocation, this.beautyLevel);
        setFloat(this.brightLocation, this.brightLevel);
    }


}
