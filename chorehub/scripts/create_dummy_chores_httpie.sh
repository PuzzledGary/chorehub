#!/usr/bin/env bash
# HTTPie script: create dummy chores in ChoreHub
# Usage: install httpie (https://httpie.io/) and run:
#   bash create_dummy_chores_httpie.sh

set -euo pipefail

BASE_URL="http://homeassistant:8080/chores"

printf "Creating dummy chores against %s\n" "$BASE_URL"

printf "\n1) One-time chore (no recurrencePattern)\n"
http --json POST "$BASE_URL" \
  name="One-time Demo Chore" \
  description="Run once (demo)" \
  recurrenceType="ONETIME" \
  recurrencePattern:=null \
  assignedUsername:=null

printf "\n2) FIXED_SCHEDULE weekly chore (assigned to alice) - cron expression for Mon 08:00\n"
# Cron format (6 fields): second minute hour day month day-of-week
http --json POST "$BASE_URL" \
  name="Take out trash" \
  description="Weekly trash pickup" \
  recurrenceType="FIXED_SCHEDULE" \
  recurrencePattern="0 0 8 ? * MON" 

printf "\n3) FIXED_SCHEDULE daily chore (assigned to bob) - daily at 07:30\n"
http --json POST "$BASE_URL" \
  name="Morning dishes" \
  description="Do dishes after breakfast" \
  recurrenceType="FIXED_SCHEDULE" \
  recurrencePattern="0 30 7 * * ?"

printf "\n4) Flexible chore (no assigned user) - ISO period P7D\n"
http --json POST "$BASE_URL" \
  name="Water plants" \
  description="Flexible within 7 days" \
  recurrenceType="AFTER_COMPLETION" \
  recurrencePattern="P7D" \
  assignedUsername:=null

printf "\n5) FIXED_SCHEDULE monthly chore (assigned to alice) - 1st of month at 00:00\n"
http --json POST "$BASE_URL" \
  name="Pay rent" \
  description="Monthly payment reminder" \
  recurrenceType="FIXED_SCHEDULE" \
  recurrencePattern="0 0 0 1 * ?"
  

printf "\n6) Flexible short-interval chore (assigned to bob) - ISO period P3D\n"
http --json POST "$BASE_URL" \
  name="Vacuum living room" \
  description="Flexible within 3 days" \
  recurrenceType="AFTER_COMPLETION" \
  recurrencePattern="P3D"
  
printf "\nDone. Review responses above to get created chore IDs.\n"
