# Generated by Django 3.0.3 on 2020-04-28 15:38

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('stats', '0002_auto_20200426_0048'),
    ]

    operations = [
        migrations.AlterModelOptions(
            name='readingtime',
            options={'ordering': ['classroom', 'material', 'student', 'updating_time', 'duration']},
        ),
    ]
