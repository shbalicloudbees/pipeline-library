// vars/gcpCloudStorageDeploy.groovy
def call(String bucket, Closure body) {
  def podYaml = libraryResource 'podtemplates/google-cloud-sdk.yml'
  def label = "gcp-sdk-${UUID.randomUUID().toString()}"
  def CLOUD_RUN_URL
  podTemplate(name: 'gcp-sdk', label: label, yaml: podYaml) {
    node(label) {
      body()
      container(name: 'gcp-sdk') {
        sh "gsutil -m cp -r public/** gs://${bucket}"
      }
    }
  }
}
