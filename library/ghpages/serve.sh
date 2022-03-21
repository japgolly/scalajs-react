#!/bin/bash
cd "$(dirname "$0")" || exit 1
exec python3 serve.py
