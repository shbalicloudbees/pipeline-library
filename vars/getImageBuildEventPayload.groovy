// vars/getImageBuildEventPayload.groovy
//will get the image build payload values from a imageBuild published event and set them as environment variables
def call() {
    env.IMAGE_NAME = currentBuild?.getBuildCauses()[0]?.event?.name?.toString()
    env.IMAGE_TAG = currentBuild?.getBuildCauses()[0]?.event?.tag?.toString()
    sh "echo ${IMAGE_NAME}"
    sh "echo ${IMAGE_TAG}"
}