cmake_minimum_required(VERSION 3.4.1)

# project(hello-jn LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-deprecated-declarations \
                    -Wno-narrowing")

#OPENCV
include_directories(${OpenCV_DIR}/jni/include)
add_library(lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${OpenCV_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so)

#mobile ffmpeg
#set(build_DIR /mnt/F/Ubuntu/dummy_jni_ffmpeg/app/build/)
#/mnt/F/Ubuntu/dummy_jni_ffmpeg/app/src/main/cpp/
set(build_DIR ${CMAKE_SOURCE_DIR}/../../../build)
add_library(lib_mobileffmpeg SHARED IMPORTED)
add_library(lib_avutil SHARED IMPORTED)
if(ANDROID_ABI STREQUAL "armeabi-v7a")
    set_target_properties(lib_mobileffmpeg PROPERTIES IMPORTED_LOCATION ${build_DIR}/mobile-ffmpeg-full-gpl-4.4.aar/jni/${ANDROID_ABI}/libmobileffmpeg_armv7a_neon.so)
    set_target_properties(lib_avutil       PROPERTIES IMPORTED_LOCATION ${build_DIR}/mobile-ffmpeg-full-gpl-4.4.aar/jni/${ANDROID_ABI}/libavutil_neon.so )
else()
    set_target_properties(lib_mobileffmpeg PROPERTIES IMPORTED_LOCATION ${build_DIR}/mobile-ffmpeg-full-gpl-4.4.aar/jni/${ANDROID_ABI}/libmobileffmpeg.so)
    set_target_properties(lib_avutil       PROPERTIES IMPORTED_LOCATION ${build_DIR}/mobile-ffmpeg-full-gpl-4.4.aar/jni/${ANDROID_ABI}/libavutil.so )
endif()
add_library(hello-jni SHARED hello-jni.cpp video_writer.cpp)

target_compile_options(hello-jni PRIVATE
        "$<$<CONFIG:RELEASE>:-O3>"
        "$<$<CONFIG:DEBUG>:-O3>"
)

# Include libraries needed for hello-jni lib
target_link_libraries(
    hello-jni
    android
    log
    lib_opencv
    lib_mobileffmpeg
    lib_avutil
)
