# Resampling with Original Image Overlay and Aliasing

## Dataset

-   Single Object Detection: dataset
-   Multiple Object Detection: multi_object_test

Note: This assignment uses .rgb files to get the raw pixels for images. The above dataset folder contains .rbg files which will be given as an input to this assignment. `Visuals` folder contains it's corresponding .png files for better visualization.

## Instructions

-   To detect single object in an image:
    `./scriptSolo.sh image_path object_path`

    -   Example: `./scriptSolo.sh ./dataset/Apple_image.rgb ./dataset/Apple_object.rgb`

-   To detect multiple object in an image:
    `./scriptSolo.sh image_path object_1_path object_2_path .... object_n_path`

    -   Example: `./scriptSolo.sh ./multi_object_test/Kirby_Warning.rgb ./dataset/Kirby_object.rgb ./dataset/warning_object.rgb`

-   Run `./script.sh` for all available single object detection tests and `./scriptMultiple.sh` for all available multiple object detection tests.

-   All the logging can be found inside `logs` folder.
