# Image Processing Functions

This project contains various image-processing functions implemented in both Java and Python. 
The functions include cropping, resizing, rotating, and applying Gaussian blur to images. 
Each function is organized into its own directory with separate implementations for Java and Python.
Individual functions are executed and run using AWS to showcase our understanding of deploying Lambda functions
through Amazon Web Services (AWS). 


### Crop

- **Java Functions**: [Crop/java_functions](Crop/java_functions)
- **Python Functions**: [Crop/python_functions](Crop/python_functions)
- **Tests**: [Crop/test](Crop/test)
- **author**: Louis Lomboy
- 
### Gaussian Blur

- **Java Functions**: [Gaussian_Blur/java_functions](Gaussian_Blur/java_functions)
- **Python Functions**: [Gaussian_Blur/python-functions](Gaussian_Blur/python-functions)
- **Tests**: [Gaussian_Blur/test](Gaussian_Blur/test)
- **author**: Jovany Cardoza-Aguilar
### Resize Image

- **Java Functions**: [ResizeImage/java_functions](ResizeImage/java_functions)
- **Python Functions**: [ResizeImage/python_functions](ResizeImage/python_functions)
- **Tests**: [ResizeImage/test](ResizeImage/test)
- - **author**: Maliha Hossain

### Rotate Functions

- **Java Functions**: [rotate_functions/java_functions](rotate_functions/java_functions)
- **Python Functions**: [rotate_functions/python_functions](rotate_functions/python_functions)
- **author**: Conner Webber

## Deployment

The SAAF (Serverless Application Analytics Framework) deployment tools allow each function to be deployed to Amazon Web Services (AWS).
The deployment scripts and configuration files are located in the `deploy` directories within each function's folder.

### Using SAAF Deployment Tools

#### Java

Refer to the README files in the `deploy` directories for Java functions:

- [Crop/java_functions/deploy/README.md](Crop/java_functions/deploy/README.md)
- [Gaussian_Blur/java_functions/deploy/README.md](Gaussian_Blur/java_functions/deploy/README.md)
- [ResizeImage/java_functions/deploy/README.md](ResizeImage/java_functions/deploy/README.md)
- [rotate_functions/java_functions/deploy/README.md](rotate_functions/java_functions/deploy/README.md)

#### Python

Refer to the README files in the `deploy` directories for Python functions:

- [Crop/python_functions/deploy/README.md](Crop/python_functions/deploy/README.md)
- [Gaussian_Blur/python-functions/python_template/deploy/README.md](Gaussian_Blur/python-functions/python_template/deploy/README.md)
- [ResizeImage/python_functions/deploy/README.md](ResizeImage/python_functions/deploy/README.md)

## License

This project is licensed under the Apache License, Version 2.0. See the LICENSE files in each function's directory for more details.

- [Crop/python_functions/LICENSE](Crop/python_functions/LICENSE)
- [Gaussian_Blur/python-functions/README.md](Gaussian_Blur/python-functions/README.md)
- [ResizeImage/python_functions/src/pillow-11.0.0.dist-info/LICENSE](ResizeImage/python_functions/src/pillow-11.0.0.dist-info/LICENSE)

## Acknowledgements

Tools were provided by our instructor, Wes J. Lloyd
https://github.com/wlloyduw/SAAF
