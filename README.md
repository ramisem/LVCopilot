# LVCopilot

LVCopilot is an autonomous LabVantage (LV) Senior Developer agent powered by Google's Gemini API. It acts as an interactive AI assistant that operates in a strict three-stage loop to ensure technical accuracy and architectural alignment when developing LabVantage solutions.

## Features

- **Autonomous Agent**: Powered by `google-generativeai` to assist with LabVantage development.
- **Three-Stage Operation Loop**:
  1. **Phase 1: High-Level Requirement Elicitation**: Understands the business requirement.
  2. **Phase 2: Technical Task Scoping**: Proposes a high-level plan determining which components are needed.
- **Phase 3: Development & Implementation**: Generates Java Source Code, ensures best practice compliance (SafeSQL, try/catch/finally for AJAX, requires* flags for SDC Rules), and provides a configuration guide.
- **Knowledge Base Integration**: Loads contextual knowledge from markdown files located in the `md_files` and `skills` directories to enforce syntax, logic, and best practices.
- **Smart Context Referencing**: Use the `@` symbol in your prompts to seamlessly inject file or folder contents into the agent's context (e.g., `@src/main.java`).
- **Global CLI Access**: Install it once and use the `lvcopilot` command anywhere on your machine.
- **Model Checking Utility**: Includes a script to list available Gemini models that support content generation for your API key.

## Project Structure

- `main.py`: The entry point for the interactive CLI agent.
- `agent.py`: Contains the `LVDeveloperAgent` class which handles the Gemini API integration, knowledge base loading, and the conversation loop.
- `setup.py`: The package configuration file that bundles the CLI tool and local data files.
- `check_models.py`: A utility script to check which Gemini models are available and support `generateContent`.
- `requirements.txt`: Python dependencies needed to run the project.
- `md_files/` & `skills/`: Directories containing the markdown files that make up the agent's knowledge base (e.g., action, sdc_rule, ajax, javascript, sdms).

## Prerequisites

- Python 3.7+
- A Google Gemini API Key

## Setup & Installation

1. **Navigate to the project directory:**

   ```bash
   cd /path/to/LVCopilot
   ```

2. **Create and activate a virtual environment (recommended):**

   ```bash
   python -m venv .venv
   source .venv/bin/activate  # On Windows use: .venv\Scripts\activate
   ```

3. **Install the CLI package in editable mode:**

   Make sure you include the dot (`.`) at the end of the command!
   ```bash
   pip3 install -e .
   ```
   *(If `pip3` is not found, try `python3 -m pip install -e .`)*

4. **Configure your API Key:**

   You no longer need to manually create a `.env` file! Just run the CLI tool in any directory. If an API key is missing for that specific project, it will prompt you securely in the terminal and automatically generate the `.env` file for you.

## Usage

### Run the Agent

To start interacting with the LVCopilot agent from **any** LabVantage project directory on your machine, simply run:

```bash
lvcopilot
```

You will be prompted as the "Architect" to provide business requirements and technical scopes. The agent will guide you through its three-phase loop. Type `exit` or `quit` to end the session.

### Inject Context Using `@`
You can provide the agent with local files or folder contents directly from your prompt by prefixing the path with `@`.
```text
👤 Architect: Please modify the logic in @src/main.java to match the new business rule.
```
The agent will automatically locate `src/main.java`, read its contents, and append it to its internal context before generating a response.

### Check Available Models

To verify your API key and see which Gemini models are available for generation, run:

```bash
python check_models.py
```
