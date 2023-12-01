# Resampling with Original Image Overlay and Aliasing

## Dataset

-   HW Part 2: HD_data_samples: 7680 x 4320 HD images
-   HW Part 3: data_samples: 1920 x 1080 images

Note: This assignment uses .rgb files to get the raw pixels for images. The above dataset folder also contains .png files for visualization.

## Instructions

-   Run following command to execute part 2 of this HW.
    `./script.sh image_path, scaling_factor, anti_aliasing, window_size`

    -   For example, `./script.sh ./HD_data_samples/aliasing_test2.rgb 0.1 1 200  `, where `./HD_data_samples/aliasing_test2.rgb` is the image path, `0.1` is the scaling factor, 1 represent to do anti aliasing and 200 is the window size.

-   To execute part 3 of this HW, first make sure you are inside `HW Part 3` directory. After that, run `./scriptPart3.sh` to generate results.png.
