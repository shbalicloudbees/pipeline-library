// vars/gcpCloudStorageDeploy.groovy
def call(String bucket, Closure body) {
  def podYaml = libraryResource 'podtemplates/gsutil.yml'
  def label = "gsutil-${UUID.randomUUID().toString()}"
  def CLOUD_RUN_URL
  podTemplate(name: 'gsutil', label: label, yaml: podYaml) {
    node(label) {
      body()
      container(name: 'gsutil') {
        sh "gsutil -m cp -a -r public/** gs://${bucket}"
      }
    }
  }
}
