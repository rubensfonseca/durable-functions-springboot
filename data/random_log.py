import csv
import random
from datetime import datetime, timedelta

def generate_random_unix_date(start_date, end_date):
    random_date = start_date + timedelta(seconds=random.randrange(0, int((end_date - start_date).total_seconds())))
    return int(random_date.timestamp())

def generate_random_alphanumeric(length=6):
    characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return ''.join(random.choice(characters) for _ in range(length))

def generate_random_numeric(length=5):
    return ''.join(random.choice("0123456789") for _ in range(length))

def generate_random_alphanumeric_list(size=1000, length=6):
    return [generate_random_alphanumeric(length) for _ in range(size)]

def generate_fictional_log():
    # Set start_date to the current day at 06:00 AM
    start_date = datetime.now().replace(hour=6, minute=0, second=0, microsecond=0)

    # Set end_date to the current day at 11:00 PM
    end_date = datetime.now().replace(hour=23, minute=0, second=0, microsecond=0)

    alphanumeric_list = generate_random_alphanumeric_list()

    with open('fictional_log.txt', 'w', newline='') as txtfile:
        fieldnames = ['Unix Date', 'Alphanumeric Code', 'Numeric']
        writer = csv.DictWriter(txtfile, fieldnames=fieldnames, delimiter=' ')

        # Print the Unix date at the beginning of the day
        writer.writerow({'Unix Date': int(start_date.timestamp()), 'Alphanumeric Code': '', 'Numeric': ''})
        numeric_value = generate_random_numeric()

        rows = []
        for _ in range(100):
            unix_date = generate_random_unix_date(start_date, end_date)
            alphanumeric_code = random.choice(alphanumeric_list)

            rows.append({'Unix Date': unix_date, 'Alphanumeric Code': alphanumeric_code, 'Numeric': numeric_value})

        # Sort the rows by the 'Unix Date' column
        sorted_rows = sorted(rows, key=lambda x: x['Unix Date'])

        for row in sorted_rows:
            writer.writerow(row)

if __name__ == "__main__":
    generate_fictional_log()
