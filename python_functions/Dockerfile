FROM public.ecr.aws/lambda/python:3.13

#Copy requirements.txt
COPY requirement.txt ${LAMBDA_TASK_ROOT}

#Install specified packages
RUN pip install -r requirement.txt

#Copy all files in the current directory to the Lambda root
COPY src/* ${LAMBDA_TASK_ROOT}/

#Set the CMD to your handler
CMD [ "handler.cropFunction" ]