// vars/gcpCloudStorageDeploy.groovy
def call(Map config, Closure body) {
  def podYaml = libraryResource 'podtemplates/google-cloud-sdk.yml'
  def label = "cloud-sdk-${UUID.randomUUID().toString()}"
  def CLOUD_RUN_URL
  podTemplate(name: 'cloud-sdk', label: label, yaml: podYaml) {
    node(label) {
      body()
      container(name: 'gcp-sdk') {
        sh "gsutil -m cp -r public/** gs://${config.bucket}"
      }
    }
  }
}
