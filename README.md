# kotlin-ext-android

常用的简化`android-sdk-framework-api`的`kotlin`实现
<br>
## 如何引用
`gradle kotlin dsl`:
```
repositories {
    //https://maven.aliyun.com/mvn/view
    jcenter { url = uri("https://maven.aliyun.com/repository/jcenter") }
    google()
}

dependencies {
    implementation("com.fpliu:kotlin-ext-android:1.0.0")
}
```
其他方式请看[bintray](https://bintray.com/fpliu/newton/kotlin-ext-android)

## 文档
##### fun Application.globalReplaceFont(fontFileRelativeToAssetsDir: String)
全局替换字体。在`Application.onCreate()`中调用。
`fontFileRelativeToAssetsDir`是相对于`assets`目录的路径。
<br><br>

