from setuptools import setup, find_packages
import os

def get_data_files():
    data_files = []
    # Package md_files and skills directories into lvcopilot_data in sys.prefix
    for d in ['lvcopilot/md_files', 'lvcopilot/skills']:
        if os.path.exists(d):
            files = [os.path.join(d, f) for f in os.listdir(d) if os.path.isfile(os.path.join(d, f))]
            if files:
                dest_d = d.replace('lvcopilot/', '')
                data_files.append((f'lvcopilot_data/{dest_d}', files))
    return data_files

setup(
    name='lvcopilot',
    version='0.1.0',
    description='Autonomous LabVantage Developer Agent',
    packages=find_packages(),
    install_requires=[
        'google-generativeai',
        'python-dotenv'
    ],
    entry_points={
        'console_scripts': [
            'lvcopilot=lvcopilot.main:main',
        ],
    },
    data_files=get_data_files(),
)
