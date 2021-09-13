//
// Created by ruslan on 9/13/21.
//

#include "video_writer.h"

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <android/log.h>

void my_av_log_callback(void *ptr, int level, const char *fmt, va_list vargs) {
    __android_log_vprint(ANDROID_LOG_DEBUG, "FFMPEG", fmt, vargs);
}

extern "C" {
    extern "C" void av_log_set_callback(void* callback);
    int ffmpeg_execute(int argc, char **argv);
    void addExecution(long id);
    void removeExecution(long id);
}

std::vector<std::string> split(std::string s, std::string delimiter)
{
    std::vector<std::string> result;
    size_t pos = 0;
    std::string token;
    while ((pos = s.find(delimiter)) != std::string::npos) {
        token = s.substr(0, pos);
        result.push_back(token);
        s.erase(0, pos + delimiter.length());
    }
    result.push_back(s);

    return result;
}

std::atomic<bool> VideoWriter::ffmpeg_is_running(false);

VideoWriter::VideoWriter(const std::string &file_name, const cv::Size &size, const std::string &ffmpeg_params)
    : size(size)
{
    std::stringstream ss;
    ss << "-y -f rawvideo -pix_fmt rgb24 -s " << size.width << "x" << size.height << " -r 30"; // r - это fps
    input_params = ss.str();

    int res = pipe(fds);
    if (res == 0) {
        pipe_ok = true;
        std::stringstream all_params_str;
        all_params_str << "ffmpeg " << input_params << " -i " << "pipe:" << fds[0] << " " << ffmpeg_params << " " << file_name;
        cmd_params = split(all_params_str.str(), " ");
        ffmpeg_thread = std::thread(&VideoWriter::run_ffmpeg, this);
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, "FFMPEG", "Error on creation of pipe");
    }
}

VideoWriter::~VideoWriter()
{
    if (pipe_ok) {
        close(fds[1]);
        close(fds[0]);
    }

    ffmpeg_thread.join();
}

void VideoWriter::run_ffmpeg()
{
    // av_log_set_callback((void*)&my_av_log_callback);

    assert(!ffmpeg_is_running && "Only one running ffmpeg allowed at a time");

    ffmpeg_is_running = true;

    char ** argv = new char*[cmd_params.size()];
    for (auto i = 0; i < cmd_params.size(); i ++) {
        argv[i] = (char*)cmd_params[i].c_str();
    }

    ffmpeg_is_running = false;

    addExecution(0); // Может конфликтовать с джавой
    ffmpeg_execute(cmd_params.size(), argv);
    removeExecution(0);

    return;
}

bool VideoWriter::write(const cv::Mat &img)
{
    assert((img.type() == CV_8UC3) && (img.cols == size.width) && (img.rows == size.height) && "Mat has wrong size or wrong data type");
    int w = ::write(fds[1], img.data, size.height*size.width*3);
    return w == size.height*size.width*3;
}
