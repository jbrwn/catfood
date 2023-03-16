#!/bin/bash

# "Company Root CA" -> company-root-ca
to_dash_case() {
  echo $(tr -s ' ' '-' <<< "$1" | tr [:upper:] [:lower:])
}

# "Company Root CA" -> COMPANY_ROOT_CA
to_underscore_case() {
  echo $(tr -s ' ' '_' <<< "$1" | tr [:lower:] [:upper:])
}

get_ca_env_var_list() {
  echo $(printf '${%s} ' ${!CA_@})
}

