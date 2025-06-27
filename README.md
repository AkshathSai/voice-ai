# Voice AI Assistant with Spring AI

A real-time conversational voice-based AI assistant built with Spring AI that uses local models for core operations and internet connectivity for features like weather and news.

## Features

- **Real-time Voice Interaction**: Using local speech-to-text and text-to-speech
- **Wake Word Detection**: Hands-free activation using customizable wake words
- **Web UI**: Modern interface for both voice and text interaction
- **Intent Recognition**: Sophisticated NLP to understand user requests
- **Named Entity Recognition**: Extracts locations, people, and dates from queries
- **Skill System**: Modular architecture for extensible capabilities
- **User Profiles**: Support for multiple users with personalized settings
- **Local-First**: Primary AI operations run locally on your machine
- **Internet Services**: Weather forecasts, news, and other online data

## Requirements

- Java 21
- Maven 3.8+
- Ollama with Mistral model
- M1 MacBook Pro (or similar hardware)
- Minimum 16GB RAM

## Setup Instructions

### 1. Clone the Repository
git clone https://github.com/your-username/voice-ai-assistant.git cd voice-ai-assistant

### 2. Install Ollama and Pull Model
curl -fsSL https://ollama.com/install.sh | sh ollama pull mistral:latest

### 3. Download Required NLP and STT Models
mkdir -p models/opennlp
curl -LO https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip
unzip vosk-model-small-en-us-0.15.zip -d models/
curl -LO https://opennlp.sourceforge.net/models-1.5/en-token.bin
mv en-token.bin models/opennlp/

### 4. Configure API Keys
Update `src/main/resources/application.properties` with your free API keys for weather and news.

### 5. Build and Run
mvn spring-boot:run


### 6. Access the Web UI
Open your browser and navigate to: http://localhost:8080

## Usage

- Say "hey assistant" to activate voice recognition
- Type commands in the web UI
- Configure personal preferences in Settings

## Extending the Assistant

- Add new skills by implementing the `Skill` interface
- Train custom NLP models for better understanding
- Configure custom wake words

## License

MIT License