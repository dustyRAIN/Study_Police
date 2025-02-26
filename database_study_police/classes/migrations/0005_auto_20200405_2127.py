# Generated by Django 3.0.3 on 2020-04-05 15:27

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('classes', '0004_classhasstudent'),
    ]

    operations = [
        migrations.AlterField(
            model_name='classhasstudent',
            name='classroom',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='joined_class_details', to='classes.Classroom'),
        ),
        migrations.AlterField(
            model_name='classhasstudent',
            name='student',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='creator_name', to=settings.AUTH_USER_MODEL),
        ),
    ]
