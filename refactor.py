import os
import shutil

def main():
    os.makedirs('lvcopilot', exist_ok=True)
    
    files_to_move = ['agent.py', 'main.py', 'check_models.py']
    dirs_to_move = ['md_files', 'skills']
    
    for f in files_to_move:
        if os.path.exists(f):
            shutil.move(f, os.path.join('lvcopilot', f))
            print(f"Moved {f}")
            
    for d in dirs_to_move:
        if os.path.exists(d):
            shutil.move(d, os.path.join('lvcopilot', d))
            print(f"Moved {d}")
            
    with open('lvcopilot/__init__.py', 'w') as f:
        f.write("# Init module\n")

if __name__ == "__main__":
    main()
