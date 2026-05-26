FROM python:3.11-slim

# Set working directory
WORKDIR /app

# Install system dependencies required for building Python packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements file
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY main.py .
COPY semantic.py .
COPY image_search.py .
COPY db_utils.py .

# Validate that core AI dependencies can actually be imported in the runtime image.
RUN python -c "import faiss, torch, transformers, sentence_transformers"

# Create necessary directories
RUN mkdir -p /app/data/images /app/cache

# Expose port 8000
EXPOSE 8000

# Run the application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
