#!/usr/bin/env python

###############################
# parse a manifest file to get the repositories which has a PR or is depended by PR repo
###############################

import json
import sys
import os
import argparse
import common
from manifest import Manifest

def parse_args(args):
    """
    Parse script arguments.
    :return: Parsed args for assignment
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('--manifest-file',
                        required=True,
                        help="The file path of manifest",
                        action='store')

    parser.add_argument('--parameters-file',
                        help="The jenkins parameter file that will used for succeeding Jenkins job",
                        action='store',
                        default="downstream_parameters")

    parsed_args = parser.parse_args(args)
    return parsed_args

def get_repositories_under_test(manifest_file):
    """
    get repositories whose commit-id is something like: origin/pr/111/merge
    """
    manifest = Manifest(manifest_file)
    repos_under_test = []
    for repo in manifest.repositories:
        if "under-test" in repo:
            if repo["under-test"] is True:
                repo_name = common.strip_suffix(os.path.basename(repo["repository"]), ".git")
                repos_under_test.append(repo_name)
    return repos_under_test

def write_downstream_parameters(repos_under_test, parameters_file):
    params = {}
    params['REPOS_UNDER_TEST'] = ','.join(repos_under_test)

    # Can add customized fiter here if necessary, this is just a sample.
    repos_need_unit_test = []
    repos_need_unit_test = repos_under_test
    if len(repos_need_unit_test) > 0:
        params['REPOS_NEED_UNIT_TEST'] = ','.join(repos_need_unit_test)

    common.write_parameters(parameters_file, params)

def main():
    args = parse_args(sys.argv[1:])
    repos_under_test = get_repositories_under_test(args.manifest_file)
    write_downstream_parameters(repos_under_test, args.parameters_file)

if __name__ == '__main__':
    main()
