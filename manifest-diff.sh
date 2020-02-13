#!/bin/bash

if [ "$1" == "-h" ] || [ "$1" == "--help" ] || [ "$#" -ne 2 ]; then
  echo "------------------------------------------------------------------------------"
  echo "Usage - gen3-config-diff <[repo/]namespace[:branch]> <[repo/]namespace[:branch]>"
  echo "e.g., gen3-config-diff gen3.datastage.io:stable gitops-qa/qa-datastage.planx-pla.net"
  echo ""
  echo "This script diffs the configurations of two or more gen3 commons."
  echo ""
  echo "NOTE: Requires jq https://stedolan.github.io/jq/ and vimdiff"
  echo ""
  echo "If 'repo' is not specified, it is assumed to be 'cdis-manifest'." 
  echo "If 'branch' is not specified, it is assumed to be 'master'"
  echo "-------------------------------------------------------------------------------"
  exit 0;
fi;

github_url_base="https://raw.githubusercontent.com/uc-cdis"
default_branch="master"
default_repo="cdis-manifest"
diff_tool=vimdiff
manifest_path="manifest.json"
portal_config_path="portal/gitops.json"

diff_manifest=true
diff_portal=true

declare -a urls

# Iterate over arguments
while test ${#} -gt 0
do
  # Format: [repo/]namespace[:branch]
  # Parse the repo name, if any
  # Test for presence of a slash (/) in the part of the string before the colon, if any.
  if [[ $1 =~ ^[^:]*/.*$ ]]
    then
      # If there is a slash before a colon, everything before the slash is the repo name.
      repo=$(echo $1 | sed -r 's/^([^:]*)\/.*$/\1/')
      repo_specified=true
    else
      repo=$default_repo
  fi
  echo "repo: $repo"

  # Parse the branch name, if any
  # Test for presence of a colon(':') in the string
  if [[ $1 == *:* ]]
    then 
      # If there is a colon, everything after the colon is the branch name
      branch=$(echo $1 | sed -r 's/.*:(.*)$/\1/')
      # Replace leading underscore with forward slash('/'), in case anyone tries to use
      # the quay branch syntax (e.g. feat_newfeature instead of feat/newfeature)
      branch=$(echo $branch | sed 's/_/\//' )
      branch_specified=true
    else
      branch=$default_branch
  fi
  echo "branch: $branch"

  # Parse commons namespace
  namespace=$1
  # If the repo was specified, remove the repo name from the front of the argument
  if [[ repo_specified ]]
    then
      namespace=$(echo $namespace | sed -r "s/$repo\/(.*)/\1/")
  fi
  # Remove the branch name if present
  namespace=$(echo $namespace | sed -r 's/^([^:]*):?.*$/\1/')
  echo "namespace:$namespace"

  urls+=("$github_url_base/$repo/$branch/$namespace")
  shift
done

if [[ diff_manifest ]]
  then
    manifest_a_url="${urls[0]}/$manifest_path"
    echo "Fetching manifest from $manifest_a_url..."
    manifest_a_raw=$(curl -f $manifest_a_url)
    # echo "$manifest_a_raw"
    manifest_a_formatted=$(echo "$manifest_a_raw" | jq -S '.')

    manifest_b_url="${urls[1]}/$manifest_path"
    echo "Fetching manifest from $manifest_b_url..."
    manifest_b_raw=$(curl -f $manifest_b_url)
    # echo "$manifest_b_raw"
    manifest_b_formatted=$(echo "$manifest_b_raw" | jq -S '.')
    echo "$manifest_b_formatted"

    vimdiff <(echo "$manifest_a_formatted") <(echo "$manifest_b_formatted")
fi

if [[ diff_portal ]]
  then
    portal_a_url="${urls[0]}/$portal_config_path"
    echo "Fetching portal config from $portal_a_url..."
    portal_a_raw=$(curl -f $portal_a_url)
    # echo "$portal_a_raw"
    portal_a_formatted=$(echo "$portal_a_raw" | jq -S '.')

    portal_b_url="${urls[1]}/$portal_config_path"
    echo "Fetching portal from $portal_b_url..."
    portal_b_raw=$(curl -f $portal_b_url)
    # echo "$portal_b_raw"
    portal_b_formatted=$(echo "$portal_b_raw" | jq -S '.')
    echo "$portal_b_formatted"

    vimdiff <(echo "$portal_a_formatted") <(echo "$portal_b_formatted")
fi


# # Insert branch between repo and 'manifest.json', i.e. <repo>/master/<namespace>/manifest.json
# manifest_A_branch=master
# manifest_A_path=manifest.json
# manifest_A_path=$(echo $1 | sed "s_\(.*\)/\(.*\)_\1/$manifest_A_branch/\2/$manifest_A_path_")
# # TODO: Error check here...
# manifest_A_url="$github_url_base$manifest_A_path"
# echo "fetching manifest A from: ${manifest_A_url}"
# manifest_A_raw=$(curl -s "$manifest_A_url")
# # TODO: Error check here...
# # Sort keys and prettify json
# manifest_A=$(jq -S "." <(echo "$manifest_A_raw"))
# # TODO move 'versions' to top

# vimdiff <(echo "$manifest_A") <(echo "$manifest_B")

# echo "done"
