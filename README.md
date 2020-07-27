# gen3-release-utils
Gen3 release process automation tools


## Manifest replication

This utility (replicate_manifest_config.sh) should facilitate the copy of versions (including dictionary_url, etc.) between `manifest.json` files, i.e., improving the release process so it will be more automated & less error-prone.

 - How to use it:
 Here is the syntax:
 > /bin/bash replicate_manifest_config.sh &lt;branch>/&lt;environment>/manifest.json &lt;target_manifest>

 Example:
 > % /bin/bash ../gen3-release-utils/replicate_manifest_config.sh master/internalstaging.datastage.io/manifest.json gen3.datastage.io/manifest.json

## Release Notes Generation

This utility uses the gen3git tool to generate release notes for Gen3 monthly releases

- How to use it:
    - Set environment variable GITHUB_TOKEN with your Github Personal Access Token
    - Install Python and [gen3git](https://github.com/uc-cdis/release-helper/) command line utility
    ```pip install --editable git+https://github.com/uc-cdis/release-helper.git@gen3release#egg=gen3git```
    - Update `repo_list.txt` with the repositories from which release notes need to be generated
    - Set the following environment variables in the terminal:
        - RELEASE_NAME - Release name e.g., `Core Gen3 Release 202006 (Edgewater)`
        - START_DATE - Start date in YYYY-MM-DD format
        - END_DATE - End date in YYYY-MM-DD format
        - GITHUB_TOKEN - Github personal access token
    - Execute generate_release_notes.sh
    ```./generate_release_notes.sh```

## Make new branches from existing ones

This utility is used to create integration and stable branches in multiple repos.

- How to use it:
    - Update `repo_list.txt` with the repositories where branches need to be created
<<<<<<< HEAD
    - Execute the script as follows:
    ```./make_branch.sh <sourceBranchName> <targetBranchName```
=======
    - Execute the script as follows:
    ```./make_branch.sh <sourceBranchName> <targetBranchName```
>>>>>>> 227ae9f1a94eb8af511f7601e293dcc14e4eda8e
