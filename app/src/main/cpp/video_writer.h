//
// Created by ruslan on 9/13/21.
//

#ifndef VIDEO_WRITER_H
#define VIDEO_WRITER_H

#include <opencv2/opencv.hpp>
#include <thread>
#include <atomic>

class VideoWriter {
private:
    int fds[2];
    bool pipe_ok;
    cv::Size size;
    std::string input_params;
    std::vector<std::string> cmd_params;
    std::thread ffmpeg_thread;
    void run_ffmpeg();

    static std::atomic<bool> ffmpeg_is_running;
public:
    VideoWriter(const std::string &file_name, const cv::Size &size, const std::string &ffmpeg_params);
    ~VideoWriter();
    bool write(const cv::Mat &img);
};

#endif //VIDEO_WRITER_H
