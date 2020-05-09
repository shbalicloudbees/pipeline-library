def call(githubCredentialId, repoName, repoOwner, sha, targetUrl, description, context, state="success") {        
  withCredentials([usernamePassword(credentialsId: "${githubCredentialId}", usernameVariable: 'USERNAME', passwordVariable: 'TOKEN')]) {
    sh """
      curl -s -H "Authorization: token ${TOKEN}" \
        -X POST -d '{"state": "${state}", "target_url": "${targetUrl}", "description": "${description}", "context": "cloudbees-ci/${context}"}' \
        "https://api.github.com/repos/${repoOwner}/${repo}/statuses/${sha}"
    """
  }
} 
